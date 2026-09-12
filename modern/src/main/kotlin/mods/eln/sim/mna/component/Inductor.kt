package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessI
import mods.eln.sim.mna.state.CurrentState
import mods.eln.sim.mna.state.State

class Inductor(
    val name: String,
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin), ISubSystemProcessI {
    var inductance: Double = 0.0
        private set

    private var inductancePerStep = 0.0

    val currentState = CurrentState()

    override val current: Double
        get() = currentState.state

    val energy: Double
        get() = current * current * inductance / 2.0

    fun setInductance(inductance: Double): Inductor = apply {
        this.inductance = inductance
        dirty()
    }

    override fun applyToSubsystem(subSystem: SubSystem) {
        inductancePerStep = -inductance / subSystem.dt
        subSystem.addToA(aPin, currentState, 1.0)
        subSystem.addToA(bPin, currentState, -1.0)
        subSystem.addToA(currentState, aPin, 1.0)
        subSystem.addToA(currentState, bPin, -1.0)
        subSystem.addToA(currentState, currentState, inductancePerStep)
    }

    override fun simProcessI(subSystem: SubSystem) {
        subSystem.addToI(currentState, inductancePerStep * currentState.state)
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

    fun resetStates() {
        currentState.state = 0.0
    }
}
