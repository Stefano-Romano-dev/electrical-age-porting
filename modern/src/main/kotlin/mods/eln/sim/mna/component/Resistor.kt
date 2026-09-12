package mods.eln.sim.mna.component

import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.State

open class Resistor(
    aPin: State? = null,
    bPin: State? = null,
) : Bipole(aPin, bPin) {
    var resistance: Double = MnaConst.HIGH_IMPEDANCE
        private set

    private var inverseResistance: Double = 1.0 / resistance

    override val current: Double
        get() = voltage * inverseResistance

    val power: Double
        get() = voltage * current

    open fun setResistance(resistance: Double): Resistor = apply {
        if (!resistance.isFinite()) return@apply
        if (this.resistance != resistance) {
            this.resistance = resistance
            inverseResistance = 1.0 / resistance
            dirty()
        }
    }

    open fun highImpedance() {
        setResistance(MnaConst.HIGH_IMPEDANCE)
    }

    fun pullDown(): Resistor = setResistance(MnaConst.PULL_DOWN)

    override fun applyToSubsystem(subSystem: SubSystem) {
        subSystem.addToA(aPin, aPin, inverseResistance)
        subSystem.addToA(aPin, bPin, -inverseResistance)
        subSystem.addToA(bPin, bPin, inverseResistance)
        subSystem.addToA(bPin, aPin, -inverseResistance)
    }
}
