package mods.eln.sim.process.destruct

import mods.eln.sim.ThermalLoad
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import java.util.Locale

fun interface WatchdogDiagnosticSink {
    fun dump(target: Any, reason: String)

    companion object {
        @JvmField
        val NONE = WatchdogDiagnosticSink { _, _ -> }
    }
}

data class ThermalWatchdogTrip(
    val absoluteTemperature: Double,
    val thermalDeltaCelsius: Double,
    val ambientCelsius: Double,
    val maximum: Double,
    val minimum: Double,
    val overflow: Double,
    val deltaPerSecond: Double,
    val thermalPower: Double,
    val heatCapacity: Double,
)

class ThermalLoadWatchDog(
    var state: ThermalLoad,
    policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    randomFactor: WatchdogRandomFactor = WatchdogRandomFactor.LEGACY_UNIFORM,
    private val diagnosticSink: WatchdogDiagnosticSink = WatchdogDiagnosticSink.NONE,
    private val tripObserver: (ThermalWatchdogTrip) -> Unit = {},
) : ValueWatchdog(policy, randomFactor) {
    private var type = WatchdogType.THERMAL
    override val watchdogType: WatchdogType
        get() = type

    private var lastTemperature = state.getTemperature()
    private var lastDeltaPerSecond = 0.0
    private var matrixDumpSupplier: (() -> Any?)? = null
    private var matrixDumpReason: String? = null
    private var ambientTemperatureProvider: (() -> Double)? = null

    override fun getValue(): Double = state.getTemperature() + (ambientTemperatureProvider?.invoke() ?: 0.0)

    override fun process(time: Double) {
        lastDeltaPerSecond = if (time > 0.0) {
            (state.getTemperature() - lastTemperature) / time
        } else {
            0.0
        }
        lastTemperature = state.getTemperature()
        super.process(time)
    }

    override fun onDestroy(value: Double, overflow: Double) {
        val ambientCelsius = ambientTemperatureProvider?.invoke() ?: 0.0
        tripObserver(
            ThermalWatchdogTrip(
                absoluteTemperature = value,
                thermalDeltaCelsius = state.getTemperature(),
                ambientCelsius = ambientCelsius,
                maximum = max,
                minimum = min,
                overflow = overflow,
                deltaPerSecond = lastDeltaPerSecond,
                thermalPower = state.Pc,
                heatCapacity = state.heatCapacity,
            ),
        )
        matrixDumpSupplier?.let { supplier ->
            runCatching { supplier() }.getOrNull()?.let { target ->
                val reason = matrixDumpReason ?: String.format(Locale.ROOT, "Thermal watchdog %.1f°C", value)
                diagnosticSink.dump(target, reason)
            }
        }
    }

    fun setMaximumTemperature(maximumTemperature: Double): ThermalLoadWatchDog = apply {
        max = maximumTemperature
        min = -40.0
        timeoutReset = maximumTemperature
    }

    fun setThermalLoad(initializer: ThermalLoadInitializer): ThermalLoadWatchDog = apply {
        max = initializer.maximumTemperature
        min = initializer.minimumTemperature
        timeoutReset = max
    }

    fun setTemperatureLimits(maximumTemperature: Double, minimumTemperature: Double): ThermalLoadWatchDog = apply {
        max = maximumTemperature
        min = minimumTemperature
        timeoutReset = max
    }

    fun setTemperatureLimits(initializer: ThermalLoadInitializerByPowerDrop): ThermalLoadWatchDog = apply {
        max = initializer.maximumTemperature
        min = initializer.minimumTemperature
        timeoutReset = max
    }

    fun dumpMatrixOnTrip(reason: String? = null, supplier: () -> Any?): ThermalLoadWatchDog = apply {
        matrixDumpReason = reason
        matrixDumpSupplier = supplier
    }

    fun setAmbientTemperatureProvider(provider: () -> Double): ThermalLoadWatchDog = apply {
        ambientTemperatureProvider = provider
    }

    fun asResistorHeatWatchdog(): ThermalLoadWatchDog = apply {
        type = WatchdogType.RESISTOR_HEAT
    }
}
