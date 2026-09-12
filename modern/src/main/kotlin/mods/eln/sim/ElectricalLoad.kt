package mods.eln.sim

import mods.eln.sim.mna.component.Bipole
import mods.eln.sim.mna.component.Line
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.State
import mods.eln.sim.mna.state.VoltageStateLineReady
import kotlin.math.abs

open class ElectricalLoad : VoltageStateLineReady() {
    var serialResistance: Double = MnaConst.HIGH_IMPEDANCE
        private set

    fun setSerialResistance(serialResistance: Double) {
        if (this.serialResistance != serialResistance) {
            this.serialResistance = serialResistance
            connectedComponents()
                .filterIsInstance<ElectricalConnection>()
                .forEach(ElectricalConnection::notifyRsChange)
        }
    }

    fun highImpedance() {
        setSerialResistance(MnaConst.HIGH_IMPEDANCE)
    }

    val current: Double
        get() = connectedComponents()
            .asSequence()
            .filterIsInstance<Bipole>()
            .filterNot { it is Line }
            .sumOf { abs(it.current) } * 0.5

    companion object {
        @JvmField
        val groundLoad: State? = null
    }
}
