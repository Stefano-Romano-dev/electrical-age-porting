package mods.eln.sim

object PhysicalConstant {
    const val ZERO_CELSIUS_IN_KELVIN: Double = 273.15

    // Legacy source compatibility for callers ported incrementally.
    const val zeroCelsiusInKelvin: Double = ZERO_CELSIUS_IN_KELVIN
}
