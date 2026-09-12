package mods.eln.sim.mna.process

import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.state.State

class TransformerInterSystemProcess(
    private val aState: State,
    private val bState: State,
    private val aVoltageSource: VoltageSource,
    private val bVoltageSource: VoltageSource,
) : IRootSystemPreStepProcess {
    var ratio: Double = 1.0
        private set

    fun setRatio(ratio: Double) {
        this.ratio = ratio
    }

    override fun rootSystemPreStepProcess() {
        val a = requireNotNull(aVoltageSource.subSystem).getTh(aState, aVoltageSource)
        val b = requireNotNull(bVoltageSource.subSystem).getTh(bState, bVoltageSource)
        var voltage = (a.voltage * b.resistance + ratio * b.voltage * a.resistance) /
            (b.resistance + ratio * ratio * a.resistance)
        if (voltage.isNaN()) voltage = 0.0
        aVoltageSource.setVoltage(voltage)
        bVoltageSource.setVoltage(voltage * ratio)
    }
}
