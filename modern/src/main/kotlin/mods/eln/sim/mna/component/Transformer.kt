package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.state.CurrentState
import mods.eln.sim.mna.state.State

class Transformer(
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin) {
    val aCurrentState = CurrentState()
    val bCurrentState = CurrentState()

    var ratio: Double = 1.0
        private set

    override val current: Double
        get() = 0.0

    fun setRatio(ratio: Double): Transformer = apply {
        this.ratio = ratio
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        subSystem.addState(aCurrentState)
        subSystem.addState(bCurrentState)
    }

    override fun quitSubSystem() {
        directSubSystem?.removeState(aCurrentState)
        directSubSystem?.removeState(bCurrentState)
        super.quitSubSystem()
    }

    override fun applyToSubsystem(subSystem: SubSystem) {
        subSystem.addToA(bPin, bCurrentState, 1.0)
        subSystem.addToA(bCurrentState, bPin, 1.0)
        subSystem.addToA(bCurrentState, aPin, -ratio)

        subSystem.addToA(aPin, aCurrentState, 1.0)
        subSystem.addToA(aCurrentState, aPin, 1.0)
        subSystem.addToA(aCurrentState, bPin, -1.0 / ratio)

        subSystem.addToA(aCurrentState, aCurrentState, 1.0)
        subSystem.addToA(aCurrentState, bCurrentState, ratio)
        subSystem.addToA(bCurrentState, aCurrentState, 1.0)
        subSystem.addToA(bCurrentState, bCurrentState, ratio)
    }
}
