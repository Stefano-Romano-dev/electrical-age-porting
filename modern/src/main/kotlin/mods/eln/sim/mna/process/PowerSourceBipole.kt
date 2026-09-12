package mods.eln.sim.mna.process

import kotlin.math.sqrt
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sim.mna.state.State

class PowerSourceBipole(
    private val aPin: State,
    private val bPin: State,
    private val aSource: VoltageSource,
    private val bSource: VoltageSource,
) : IRootSystemPreStepProcess {
    var power: Double = 0.0
        private set

    var maximumVoltage: Double = 0.0
        private set

    var maximumCurrent: Double = 0.0
        private set

    fun setPower(power: Double) {
        this.power = power
    }

    fun setMaximums(maximumVoltage: Double, maximumCurrent: Double) {
        this.maximumVoltage = maximumVoltage
        this.maximumCurrent = maximumCurrent
    }

    fun setMaximumVoltage(maximumVoltage: Double) {
        this.maximumVoltage = maximumVoltage
    }

    fun setMaximumCurrent(maximumCurrent: Double) {
        this.maximumCurrent = maximumCurrent
    }

    override fun rootSystemPreStepProcess() {
        val a = requireNotNull(aPin.subSystem).getTh(aPin, aSource).sanitize()
        val b = requireNotNull(bPin.subSystem).getTh(bPin, bSource).sanitize()
        val theveninVoltage = a.voltage - b.voltage
        val theveninResistance = a.resistance + b.resistance

        if (theveninVoltage >= maximumVoltage) {
            aSource.setVoltage(a.voltage)
            bSource.setVoltage(b.voltage)
            return
        }

        var voltage = (sqrt(theveninVoltage * theveninVoltage + 4.0 * power * theveninResistance) +
            theveninVoltage) / 2.0
        voltage = minOf(voltage, maximumVoltage, theveninVoltage + theveninResistance * maximumCurrent)
        if (voltage.isNaN()) voltage = 0.0

        val current = (theveninVoltage - voltage) / theveninResistance
        aSource.setVoltage(a.voltage - current * a.resistance)
        bSource.setVoltage(b.voltage + current * b.resistance)
    }

    private fun mods.eln.sim.mna.SubSystem.Thevenin.sanitize(): mods.eln.sim.mna.SubSystem.Thevenin {
        if (voltage.isNaN()) {
            voltage = 0.0
            resistance = MnaConst.HIGH_IMPEDANCE
        }
        return this
    }
}
