package mods.eln.sim.process.heater

import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad

class ElectricalLoadHeatThermalLoad(
    var resistor: ElectricalLoad,
    var load: ThermalLoad,
) : IProcess {
    private var maxDeltaTPerSecond: Double? = null

    fun limitTemperatureRate(maxDeltaTPerSecond: Double): ElectricalLoadHeatThermalLoad = apply {
        this.maxDeltaTPerSecond = maxDeltaTPerSecond
    }

    override fun process(time: Double) {
        if (resistor.isNotSimulated) return
        val current = resistor.current
        var power = current * current * resistor.serialResistance * 2.0
        maxDeltaTPerSecond
            ?.takeIf { it.isFinite() && it > 0.0 && load.heatCapacity > 0.0 }
            ?.let { limit ->
                val maxPower = limit * load.heatCapacity
                power = power.coerceIn(-maxPower, maxPower)
            }
        load.movePowerTo(power)
    }
}
