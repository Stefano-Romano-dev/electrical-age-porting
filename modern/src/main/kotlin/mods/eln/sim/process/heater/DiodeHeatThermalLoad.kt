package mods.eln.sim.process.heater

import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor

class DiodeHeatThermalLoad(
    val resistor: Resistor,
    val load: ThermalLoad,
) : IProcess {
    private var lastResistance = resistor.resistance

    override fun process(time: Double) {
        if (resistor.resistance == lastResistance) {
            load.movePowerTo(resistor.power)
        } else {
            lastResistance = resistor.resistance
        }
    }
}
