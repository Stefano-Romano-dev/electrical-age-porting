package mods.eln.sim

import mods.eln.sim.mna.component.Resistor

class ElectricalResistorHeatThermalLoad(
    val electricalResistor: Resistor,
    val thermalLoad: ThermalLoad,
) : IProcess {
    override fun process(time: Double) {
        thermalLoad.PcTemp += electricalResistor.power
    }
}
