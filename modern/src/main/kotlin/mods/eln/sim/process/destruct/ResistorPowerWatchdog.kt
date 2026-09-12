package mods.eln.sim.process.destruct

import mods.eln.sim.mna.component.Resistor

class ResistorPowerWatchdog(
    var resistor: Resistor,
    policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    randomFactor: WatchdogRandomFactor = WatchdogRandomFactor.LEGACY_UNIFORM,
) : ValueWatchdog(policy, randomFactor) {
    override val watchdogType = WatchdogType.RESISTOR_HEAT

    fun setMaximumPower(maximumPower: Double): ResistorPowerWatchdog = apply {
        max = maximumPower
        min = -1.0
        timeoutReset = maximumPower
    }

    override fun getValue(): Double = resistor.power
}
