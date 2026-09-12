package mods.eln.sim.process.destruct

import mods.eln.sim.mna.component.Bipole

class BipoleVoltageWatchdog(
    var bipole: Bipole,
    policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    randomFactor: WatchdogRandomFactor = WatchdogRandomFactor.LEGACY_UNIFORM,
) : ValueWatchdog(policy, randomFactor) {
    override val watchdogType = WatchdogType.VOLTAGE

    fun setNominalVoltage(nominalVoltage: Double): BipoleVoltageWatchdog = apply {
        max = nominalVoltage * 1.3
        min = -max
        timeoutReset = nominalVoltage * 0.5
    }

    override fun getValue(): Double = bipole.voltage
}
