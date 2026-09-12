package mods.eln.sim.mna.component

import mods.eln.sim.mna.RootSystem
import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.IDestructor
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.state.State
import mods.eln.sim.mna.state.VoltageState

class InterSystemAbstraction(
    private val rootSystem: RootSystem,
    private val interSystemResistor: Resistor,
) : IAbstractor, IDestructor, IRootSystemPreStepProcess {
    private val aState: State = requireNotNull(interSystemResistor.aPin)
    private val bState: State = requireNotNull(interSystemResistor.bPin)
    private val aSystem: SubSystem = requireNotNull(aState.subSystem)
    private val bSystem: SubSystem = requireNotNull(bState.subSystem)

    internal val aNewState = VoltageState()
    internal val aNewResistor = Resistor()
    internal val aNewDelay = VoltageSource("inter_system_a")
    internal val bNewState = VoltageState()
    internal val bNewResistor = Resistor()
    internal val bNewDelay = VoltageSource("inter_system_b")

    init {
        require(aSystem !== bSystem) { "Inter-system endpoints must belong to different subsystems" }
        aSystem.interSystemConnectivity += bSystem
        bSystem.interSystemConnectivity += aSystem

        aNewResistor.connectGhostTo(aState, aNewState)
        aNewDelay.connectTo(aNewState, null)
        bNewResistor.connectGhostTo(bState, bNewState)
        bNewDelay.connectTo(bNewState, null)
        calibrate()

        aSystem.addComponent(aNewResistor)
        aSystem.addState(aNewState)
        aSystem.addComponent(aNewDelay)
        bSystem.addComponent(bNewResistor)
        bSystem.addState(bNewState)
        bSystem.addComponent(bNewDelay)

        aSystem.breakDestructors += this
        bSystem.breakDestructors += this
        interSystemResistor.abstractedBy = this
        rootSystem.addProcess(this)
    }

    private fun calibrate() {
        val voltage = (aState.state + bState.state) / 2.0
        aNewDelay.setVoltage(voltage)
        bNewDelay.setVoltage(voltage)
        val resistance = interSystemResistor.resistance / 2.0
        aNewResistor.setResistance(resistance)
        bNewResistor.setResistance(resistance)
    }

    override fun dirty(component: Component) {
        calibrate()
    }

    override val abstractorSubSystem: SubSystem
        get() = aSystem

    override fun destruct() {
        aSystem.breakDestructors.remove(this)
        bSystem.breakDestructors.remove(this)
        aSystem.removeComponent(aNewDelay)
        aSystem.removeComponent(aNewResistor)
        aSystem.removeState(aNewState)
        bSystem.removeComponent(bNewDelay)
        bSystem.removeComponent(bNewResistor)
        bSystem.removeState(bNewState)
        rootSystem.removeProcess(this)
        interSystemResistor.abstractedBy = null
        aSystem.restoreAbstractedComponent(interSystemResistor)
    }

    override fun rootSystemPreStepProcess() {
        val a = requireNotNull(aNewDelay.subSystem).getTh(aState, aNewDelay)
        val b = requireNotNull(bNewDelay.subSystem).getTh(bState, bNewDelay)
        var voltage = (a.voltage - b.voltage) * b.resistance / (a.resistance + b.resistance) + b.voltage
        if (voltage.isNaN()) voltage = 0.0
        aNewDelay.setVoltage(voltage)
        bNewDelay.setVoltage(voltage)
    }
}
