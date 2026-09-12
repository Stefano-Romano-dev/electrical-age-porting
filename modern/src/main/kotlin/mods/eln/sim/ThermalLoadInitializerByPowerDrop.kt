@file:Suppress("PropertyName")

package mods.eln.sim

class ThermalLoadInitializerByPowerDrop(
    var maximumTemperature: Double,
    var minimumTemperature: Double,
    private val heatingTao: Double,
    private val TConductivityDrop: Double,
    private val validator: ThermalLoadValidator,
) {
    var Rs: Double = 0.0
        private set
    var Rp: Double = 0.0
        private set
    var C: Double = 0.0
        private set

    fun setMaximalPower(power: Double) {
        C = power * heatingTao / maximumTemperature
        Rp = maximumTemperature / power
        Rs = TConductivityDrop / power / 2.0
        validator.checkThermalLoad(Rs, Rp, C)
    }

    fun applyToThermalLoad(load: ThermalLoad) {
        load.set(Rs, Rp, C)
    }

    fun copy(): ThermalLoadInitializerByPowerDrop =
        ThermalLoadInitializerByPowerDrop(
            maximumTemperature,
            minimumTemperature,
            heatingTao,
            TConductivityDrop,
            validator,
        ).also {
            it.Rs = Rs
            it.Rp = Rp
            it.C = C
        }
}
