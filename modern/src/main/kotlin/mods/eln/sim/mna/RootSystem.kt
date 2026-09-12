package mods.eln.sim.mna

import mods.eln.sim.mna.component.Component
import mods.eln.sim.mna.component.Line
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.InterSystemAbstraction
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.misc.ISubSystemProcessFlush
import mods.eln.sim.mna.state.State

/** Owns the MNA topology and rebuilds connected subsystems when it changes. */
class RootSystem(
    val dt: Double,
    val interSystemOverSampling: Int,
) {
    init {
        require(dt > 0.0 && dt.isFinite()) { "dt must be finite and positive" }
        require(interSystemOverSampling > 0) { "interSystemOverSampling must be positive" }
    }

    val systems = mutableListOf<SubSystem>()
    val pendingComponents = linkedSetOf<Component>()
    val pendingStates = linkedSetOf<State>()

    private val flushProcesses = mutableListOf<ISubSystemProcessFlush>()
    private val preStepProcesses = mutableListOf<IRootSystemPreStepProcess>()

    val subSystemCount: Int
        get() = systems.size

    fun addComponent(component: Component) {
        pendingComponents += component
        component.onAddToRootSystem()
        component.connectedStates()
            .mapNotNull { it?.subSystem }
            .distinct()
            .forEach(::breakSystems)
    }

    fun removeComponent(component: Component) {
        component.subSystem?.let(::breakSystems)
        pendingComponents -= component
        component.onRemoveFromRootSystem()
    }

    fun addState(state: State) {
        state.connectedComponentsNotAbstracted()
            .mapNotNull { it.subSystem }
            .distinct()
            .forEach(::breakSystems)
        state.subSystem?.let(::breakSystems)
        pendingStates += state
    }

    fun removeState(state: State) {
        state.subSystem?.let(::breakSystems)
        pendingStates -= state
    }

    fun generate() {
        generateLines()
        pendingStates.filter { it.mustBeFarFromInterSystem() }.toList().forEach { state ->
            if (state in pendingStates && state.subSystem == null) buildSubSystem(state)
        }
        while (pendingStates.isNotEmpty()) {
            buildSubSystem(pendingStates.first())
        }
        generateInterSystems()
    }

    fun step() {
        generate()
        repeat(interSystemOverSampling) {
            preStepProcesses.toList().forEach { it.rootSystemPreStepProcess() }
        }
        systems.toList().forEach { it.stepCalc() }
        systems.toList().forEach { it.stepFlush() }
        flushProcesses.toList().forEach { it.simProcessFlush() }
    }

    fun breakSystems(subSystem: SubSystem) {
        if (!subSystem.breakSystem()) return
        subSystem.interSystemConnectivity.toList().forEach(::breakSystems)
    }

    fun findSubSystemWith(state: State): SubSystem? = systems.firstOrNull { it.contains(state) }

    fun isRegistered(state: State): Boolean = state.subSystem != null || state in pendingStates

    fun addProcess(process: IRootSystemPreStepProcess) {
        if (process !in preStepProcesses) preStepProcesses += process
    }

    fun removeProcess(process: IRootSystemPreStepProcess) {
        preStepProcesses -= process
    }

    fun addProcess(process: ISubSystemProcessFlush) {
        if (process !in flushProcesses) flushProcesses += process
    }

    fun removeProcess(process: ISubSystemProcessFlush) {
        flushProcesses -= process
    }

    private fun buildSubSystem(firstState: State) {
        val privateSystem = firstState.isPrivateSubSystem
        val states = linkedSetOf<State>()
        val components = linkedSetOf<Component>()
        val queue = ArrayDeque<State>()
        queue += firstState

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            if (!states.add(state)) continue

            for (component in state.connectedComponentsNotAbstracted()) {
                if (component !in pendingComponents || component.subSystem != null || component in components) continue
                if (!privateSystem && queue.size + states.size > MAX_SUBSYSTEM_SIZE && component.canBeReplacedByInterSystem()) {
                    continue
                }

                val connected = component.connectedStates().filterNotNull()
                val crossesBoundary = connected.any {
                    it.subSystem != null || it.isPrivateSubSystem != privateSystem
                }
                if (crossesBoundary) continue

                components += component
                connected.filterNot { it in states }.forEach(queue::addLast)
            }
        }

        pendingStates.removeAll(states)
        pendingComponents.removeAll(components)

        val subSystem = SubSystem(root = this, dt = dt)
        states.forEach(subSystem::addState)
        components.forEach(subSystem::addComponent)
        systems += subSystem
    }

    private fun isValidForLine(state: State): Boolean {
        if (!state.canBeSimplifiedByLine()) return false
        val connected = state.connectedComponentsNotAbstracted()
        return connected.size == 2 && connected.all { it is Resistor && it in pendingComponents }
    }

    private fun generateLines() {
        val stateScope = pendingStates.filterTo(linkedSetOf(), ::isValidForLine)
        while (stateScope.isNotEmpty()) {
            val rootState = stateScope.first()
            var statePointer = rootState
            var resistorPointer = statePointer.connectedComponentsNotAbstracted().first() as Resistor

            while (true) {
                resistorPointer = statePointer.connectedComponentsNotAbstracted()
                    .first { it !== resistorPointer } as Resistor
                val next = resistorPointer.otherPin(statePointer)
                if (next == null || next === rootState || next !in stateScope) break
                statePointer = next
            }

            val lineStates = mutableListOf<State>()
            val lineResistors = mutableListOf(resistorPointer)
            while (true) {
                lineStates += statePointer
                stateScope -= statePointer
                resistorPointer = statePointer.connectedComponentsNotAbstracted()
                    .first { it !== resistorPointer } as Resistor
                lineResistors += resistorPointer

                val next = resistorPointer.otherPin(statePointer)
                if (next == null || next !in stateScope) break
                statePointer = next
            }

            if (lineResistors.first() === lineResistors.last()) {
                lineResistors.removeFirst()
                lineStates.removeFirst()
            }
            Line.create(this, lineResistors, lineStates)
        }
    }

    internal fun generateInterSystems() {
        pendingComponents.toList().forEach { component ->
            val resistor = component as? Resistor ?: return@forEach
            val a = resistor.aPin ?: return@forEach
            val b = resistor.bPin ?: return@forEach
            val aSystem = a.subSystem ?: return@forEach
            val bSystem = b.subSystem ?: return@forEach
            if (aSystem === bSystem) return@forEach

            InterSystemAbstraction(this, resistor)
            pendingComponents -= resistor
        }
    }

    private fun Resistor.otherPin(state: State): State? = when {
        aPin !== state -> aPin
        bPin !== state -> bPin
        else -> null
    }

    private companion object {
        const val MAX_SUBSYSTEM_SIZE = 100
    }
}
