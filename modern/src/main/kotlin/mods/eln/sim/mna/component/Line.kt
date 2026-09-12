package mods.eln.sim.mna.component

import mods.eln.sim.mna.RootSystem
import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessFlush
import mods.eln.sim.mna.state.State

class Line : Resistor(), ISubSystemProcessFlush, IAbstractor {
    val resistors = mutableListOf<Resistor>()
    val states = mutableListOf<State>()

    private var containsInterSystem = false

    fun canAddComponent(component: Component): Boolean = component is Resistor

    fun addResistor(resistor: Resistor) {
        containsInterSystem = containsInterSystem || resistor.canBeReplacedByInterSystem()
        resistors += resistor
    }

    override fun canBeReplacedByInterSystem(): Boolean = containsInterSystem

    fun recalculateResistance() {
        setResistance(resistors.sumOf { it.resistance })
    }

    override fun returnToRootSystem(rootSystem: RootSystem) {
        resistors.forEach { it.abstractedBy = null }
        states.forEach { it.abstractedBy = null }
        breakConnection()
        rootSystem.pendingStates += states
        rootSystem.pendingComponents += resistors
        rootSystem.removeProcess(this)
    }

    override fun simProcessFlush() {
        val firstPin = requireNotNull(aPin) { "A line must have a first endpoint" }
        var voltage = firstPin.state
        val current = current
        states.forEachIndexed { index, state ->
            voltage -= resistors[index].resistance * current
            state.state = voltage
        }
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        subSystem.addProcess(this)
        super.addToSubsystem(subSystem)
    }

    override fun quitSubSystem() = Unit

    override fun dirty(component: Component) {
        recalculateResistance()
    }

    override val abstractorSubSystem: SubSystem?
        get() = subSystem

    companion object {
        fun create(rootSystem: RootSystem, resistors: List<Resistor>, states: List<State>): Line? {
            if (resistors.size <= 1) return null
            require(resistors.size == states.size + 1) { "A line needs one more resistor than intermediate states" }

            val first = resistors.first()
            val last = resistors.last()
            val stateBefore = if (first.aPin === states.first()) first.bPin else first.aPin
            val stateAfter = if (last.aPin === states.last()) last.bPin else last.aPin

            val line = Line()
            resistors.forEach(line::addResistor)
            line.states += states
            line.recalculateResistance()

            rootSystem.pendingComponents.removeAll(resistors.toSet())
            rootSystem.pendingStates.removeAll(states.toSet())
            rootSystem.pendingComponents += line
            line.connectTo(stateBefore, stateAfter)
            rootSystem.addProcess(line)

            resistors.forEach { it.abstractedBy = line }
            states.forEach { it.abstractedBy = line }
            return line
        }
    }
}
