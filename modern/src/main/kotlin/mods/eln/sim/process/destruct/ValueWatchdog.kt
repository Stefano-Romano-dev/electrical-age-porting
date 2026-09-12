package mods.eln.sim.process.destruct

import mods.eln.sim.IProcess
import kotlin.random.Random

enum class WatchdogType {
    THERMAL,
    RESISTOR_HEAT,
    VOLTAGE,
    SHAFT_SPEED,
    OTHER,
}

fun interface WatchdogPolicy {
    fun isEnabled(type: WatchdogType): Boolean

    companion object {
        @JvmField
        val ENABLED = WatchdogPolicy { true }

        @JvmField
        val DISABLED = WatchdogPolicy { false }
    }
}

fun interface WatchdogRandomFactor {
    fun next(): Double

    companion object {
        @JvmField
        val LEGACY_UNIFORM = WatchdogRandomFactor { Random.Default.nextDouble(0.5, 1.5) }
    }
}

abstract class ValueWatchdog(
    private val policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    private val randomFactor: WatchdogRandomFactor = WatchdogRandomFactor.LEGACY_UNIFORM,
) : IProcess {
    private var destructible: IDestructible? = null
    var min: Double = 0.0
    var max: Double = 0.0
    var timeoutReset: Double = 2.0
    var timeout: Double = 0.0
    var boot: Boolean = true

    // The legacy name was "joker": the first consecutive overflow is ignored.
    private var joker = true
    protected open val watchdogType: WatchdogType = WatchdogType.OTHER

    override fun process(time: Double) {
        if (boot) {
            boot = false
            timeout = timeoutReset
        }
        val value = getValue()
        var overflow = (value - max).coerceAtLeast(min - value)
        if (overflow > 0.0) {
            if (joker) {
                joker = false
                overflow = 0.0
            }
        } else {
            joker = true
        }
        timeout -= time * overflow * randomFactor.next()
        if (timeout > timeoutReset) timeout = timeoutReset
        if (timeout < 0.0) {
            onDestroy(value, overflow)
            if (policy.isEnabled(watchdogType)) destructible?.destructImpl()
        }
    }

    fun setDestroys(destructible: IDestructible): ValueWatchdog = apply {
        this.destructible = destructible
    }

    abstract fun getValue(): Double

    protected open fun onDestroy(value: Double, overflow: Double) = Unit

    fun reset() {
        // Legacy reset does not restore joker or timeout directly.
        boot = true
    }
}
