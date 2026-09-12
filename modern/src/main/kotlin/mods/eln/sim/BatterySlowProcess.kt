package mods.eln.sim

import kotlin.math.abs

fun interface BatteryAgingPolicy {
    fun isEnabled(): Boolean

    companion object {
        @JvmField
        val ENABLED = BatteryAgingPolicy { true }

        @JvmField
        val DISABLED = BatteryAgingPolicy { false }
    }
}

abstract class BatterySlowProcess(
    private var batteryProcess: BatteryProcess,
    var thermalLoad: ThermalLoad,
    private val agingPolicy: BatteryAgingPolicy = BatteryAgingPolicy.ENABLED,
) : IProcess {
    var lifeNominalCurrent: Double = 0.0
    var lifeNominalLost: Double = 0.0

    override fun process(time: Double) {
        val voltage = batteryProcess.u
        if (voltage < -0.1 * batteryProcess.uNominal) {
            destroy()
            return
        }
        if (voltage > maximumVoltage) {
            destroy()
            return
        }
        if (agingPolicy.isEnabled()) {
            var newLife = batteryProcess.life
            val normalizedCurrent = abs(batteryProcess.dischargeCurrent) / lifeNominalCurrent
            newLife -= normalizedCurrent * normalizedCurrent * lifeNominalLost * time
            if (newLife < 0.1) newLife = 0.1
            batteryProcess.changeLife(newLife)
        }
    }

    val maximumVoltage: Double
        get() = 1.3 * batteryProcess.uNominal

    abstract fun destroy()
}
