package mods.eln.sim.process.heater

import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor

class ResistorHeatThermalLoad(
    val resistor: Resistor,
    val load: ThermalLoad,
) : IProcess {
    override fun process(time: Double) {
        load.movePowerTo(resistor.power)
    }
}
