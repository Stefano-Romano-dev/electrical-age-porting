package mods.eln.sim.mna.component

import mods.eln.sim.mna.state.State
import mods.eln.sim.mna.state.VoltageState

abstract class Monopole : Component() {
    var pin: VoltageState? = null
        private set

    fun connectTo(pin: VoltageState?): Monopole = apply {
        this.pin = pin
        pin?.addComponent(this)
    }

    override fun connectedStates(): Array<State?> = arrayOf(pin)
}
