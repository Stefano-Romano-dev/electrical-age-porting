@file:Suppress("PropertyName")

package mods.eln.sim

import kotlin.math.abs

class ThermalLoad(
    var temperatureCelsius: Double = 0.0,
    var Rp: Double = HIGH_IMPEDANCE,
    var Rs: Double = HIGH_IMPEDANCE,
    var heatCapacity: Double = 1.0,
) {
    var Pc: Double = 0.0
    var Prs: Double = 0.0
    var Psp: Double = 0.0

    var PrsTemp: Double = 0.0
    var PspTemp: Double = 0.0
    var PcTemp: Double = 0.0

    var isSlow: Boolean = false
        private set

    private var hasSimCoordinate = false
    private var simDimension = 0
    private var simX = 0
    private var simY = 0
    private var simZ = 0

    fun setRsByTao(tao: Double) {
        Rs = tao / heatCapacity
    }

    fun setHighImpedance() {
        Rs = HIGH_IMPEDANCE
        heatCapacity = 1.0
        Rp = HIGH_IMPEDANCE
    }

    val power: Double
        get() = if (
            Prs.isNaN() || Pc.isNaN() || temperatureCelsius.isNaN() ||
            Rp.isNaN() || Psp.isNaN()
        ) {
            0.0
        } else {
            (Prs + abs(Pc) + temperatureCelsius / Rp + Psp) / 2.0
        }

    fun set(Rs: Double, Rp: Double, C: Double) {
        this.Rp = Rp
        this.Rs = Rs
        heatCapacity = C
    }

    fun movePowerTo(power: Double) {
        if (power.isNaN()) return
        PcTemp += power
        // Deliberately not abs(power): this is the observable 1.24.8 behavior.
        PspTemp += power
    }

    fun getTemperature(): Double {
        if (temperatureCelsius.isNaN()) temperatureCelsius = 0.0
        return temperatureCelsius
    }

    fun setAsSlow() {
        isSlow = true
    }

    fun setAsFast() {
        isSlow = false
    }

    fun setSimCoordinate(dimension: Int, x: Int, y: Int, z: Int): ThermalLoad = apply {
        hasSimCoordinate = true
        simDimension = dimension
        simX = x
        simY = y
        simZ = z
    }

    fun clearSimCoordinate() {
        hasSimCoordinate = false
    }

    fun hasSimCoordinate(): Boolean = hasSimCoordinate
    fun getSimDimension(): Int = simDimension
    fun getSimX(): Int = simX
    fun getSimY(): Int = simY
    fun getSimZ(): Int = simZ

    companion object {
        const val HIGH_IMPEDANCE: Double = 1_000_000_000.0

        @JvmField
        val externalLoad = ThermalLoad(0.0, 0.0, 0.0, 0.0)

        @JvmStatic
        fun moveEnergy(energy: Double, time: Double, from: ThermalLoad, to: ThermalLoad) {
            if (
                energy.isNaN() || time.isNaN() || time == 0.0 ||
                from.PcTemp.isNaN() || from.PspTemp.isNaN()
            ) return
            movePower(energy / time, from, to)
        }

        @JvmStatic
        fun movePower(power: Double, from: ThermalLoad, to: ThermalLoad) {
            if (power.isNaN() || from.PcTemp.isNaN() || from.PspTemp.isNaN()) return
            from.PcTemp -= power
            to.PcTemp += power
            val absolutePower = abs(power)
            from.PspTemp += absolutePower
            to.PspTemp += absolutePower
        }
    }
}
