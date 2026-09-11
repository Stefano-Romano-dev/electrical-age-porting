package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.ISubSystemProcessI
import mods.eln.sim.mna.state.State

class CurrentSource(
    val name: String,
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin), ISubSystemProcessI {
    override var current: Double = 0.0
        private set

    fun setCurrent(current: Double): CurrentSource = apply {
        this.current = current
    }

    override fun applyToSubsystem(subSystem: SubSystem) = Unit

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        subSystem.addProcess(this)
    }

    override fun quitSubSystem() {
        subSystem?.removeProcess(this)
        super.quitSubSystem()
    }

    override fun simProcessI(subSystem: SubSystem) {
        subSystem.addToI(aPin, current)
        subSystem.addToI(bPin, -current)
    }
}
