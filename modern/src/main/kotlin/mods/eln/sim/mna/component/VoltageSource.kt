package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessI
import mods.eln.sim.mna.state.CurrentState
import mods.eln.sim.mna.state.State

open class VoltageSource(
    val name: String,
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin), ISubSystemProcessI {
    val currentState = CurrentState()

    final override var voltage: Double = 0.0
        private set

    override val current: Double
        get() = -currentState.state

    open val power: Double
        get() = voltage * current

    fun setVoltage(voltage: Double): VoltageSource = apply {
        this.voltage = voltage
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        subSystem.addState(currentState)
        subSystem.addProcess(this)
    }

    override fun quitSubSystem() {
        directSubSystem?.removeState(currentState)
        directSubSystem?.removeProcess(this)
        super.quitSubSystem()
    }

    override fun applyToSubsystem(subSystem: SubSystem) {
        subSystem.addToA(aPin, currentState, 1.0)
        subSystem.addToA(bPin, currentState, -1.0)
        subSystem.addToA(currentState, aPin, 1.0)
        subSystem.addToA(currentState, bPin, -1.0)
    }

    override fun simProcessI(subSystem: SubSystem) {
        subSystem.addToI(currentState, voltage)
    }
}
