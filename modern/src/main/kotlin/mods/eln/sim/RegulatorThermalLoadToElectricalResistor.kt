package mods.eln.sim

import mods.eln.sim.mna.component.Resistor

class RegulatorThermalLoadToElectricalResistor(
    name: String,
    val thermalLoad: ThermalLoad,
    val electricalResistor: Resistor,
) : RegulatorProcess(name) {
    var minimumResistance: Double = 0.0

    override fun getHit(): Double = thermalLoad.temperatureCelsius

    override fun setCmd(cmd: Double) {
        when {
            cmd <= 0.001 -> electricalResistor.highImpedance()
            cmd >= 1.0 -> electricalResistor.setResistance(minimumResistance)
            else -> electricalResistor.setResistance(minimumResistance / cmd)
        }
    }
}
