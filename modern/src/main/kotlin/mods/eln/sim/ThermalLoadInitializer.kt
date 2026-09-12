@file:Suppress("PropertyName")

package mods.eln.sim

class ThermalLoadInitializer(
    var maximumTemperature: Double,
    var minimumTemperature: Double,
    private val heatingTao: Double,
    private val conductionTao: Double,
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
        Rs = conductionTao / C / 2.0
        validator.checkThermalLoad(Rs, Rp, C)
    }

    fun applyTo(load: ThermalLoad) {
        load.set(Rs, Rp, C)
    }

    fun copy(): ThermalLoadInitializer =
        ThermalLoadInitializer(maximumTemperature, minimumTemperature, heatingTao, conductionTao, validator).also {
            it.Rs = Rs
            it.Rp = Rp
            it.C = C
        }
}
