package mods.eln.sim

class ThermalResistor(
    val a: ThermalLoad,
    val b: ThermalLoad,
) : IProcess {
    var thermalResistance: Double = ThermalLoad.HIGH_IMPEDANCE
        private set

    private var thermalResistanceInverse: Double = 1.0 / thermalResistance

    override fun process(time: Double) {
        val power = (a.temperatureCelsius - b.temperatureCelsius) * thermalResistanceInverse
        a.PcTemp -= power
        b.PcTemp += power
    }

    val power: Double
        get() = (a.temperatureCelsius - b.temperatureCelsius) * thermalResistanceInverse

    fun setThermalResistance(thermalResistance: Double) {
        this.thermalResistance = thermalResistance
        thermalResistanceInverse = 1.0 / thermalResistance
    }

    fun highImpedance() {
        setThermalResistance(ThermalLoad.HIGH_IMPEDANCE)
    }
}
