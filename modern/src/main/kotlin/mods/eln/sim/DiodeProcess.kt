package mods.eln.sim

import mods.eln.sim.mna.component.ResistorSwitch

class DiodeProcess(
    val resistor: ResistorSwitch,
) : IProcess {
    override fun process(time: Double) {
        resistor.setState(resistor.voltage > 0.0)
    }
}
