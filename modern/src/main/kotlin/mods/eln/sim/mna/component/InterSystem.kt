package mods.eln.sim.mna.component

import mods.eln.sim.mna.state.State

class InterSystem(
    aPin: State? = null,
    bPin: State? = null,
) : Resistor(aPin, bPin) {
    override fun canBeReplacedByInterSystem(): Boolean = true
}
