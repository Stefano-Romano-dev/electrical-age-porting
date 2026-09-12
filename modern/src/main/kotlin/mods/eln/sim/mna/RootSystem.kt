package mods.eln.sim.mna

import mods.eln.sim.mna.component.Component
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
        while (pendingStates.isNotEmpty()) {
            buildSubSystem(pendingStates.first())
        }
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
}
