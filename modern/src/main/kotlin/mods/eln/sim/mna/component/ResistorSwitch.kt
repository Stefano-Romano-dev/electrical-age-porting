package mods.eln.sim.mna.component

import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.State

class ResistorSwitch(
    val name: String,
    aPin: State? = null,
    bPin: State? = null,
) : Resistor(aPin, bPin) {
    var state: Boolean = false
        private set

    var baseResistance: Double = 1.0
        private set

    var offResistance: Double = MnaConst.HIGH_IMPEDANCE
        private set

    fun setState(state: Boolean) {
        this.state = state
        super.setResistance(if (state) baseResistance else offResistance)
    }

    fun setOffResistance(resistance: Double) {
        offResistance = resistance
        super.setResistance(if (state) baseResistance else offResistance)
    }

    override fun highImpedance() {
        super.setResistance(offResistance)
    }

    override fun setResistance(resistance: Double): ResistorSwitch = apply {
        baseResistance = resistance
        super.setResistance(if (state) resistance else offResistance)
    }

    fun restoreState(baseResistance: Double, state: Boolean) {
        this.baseResistance = if (!baseResistance.isFinite() || baseResistance == 0.0) {
            offResistance
        } else {
            baseResistance
        }
        this.state = state
        super.setResistance(if (state) this.baseResistance else offResistance)
    }
}
