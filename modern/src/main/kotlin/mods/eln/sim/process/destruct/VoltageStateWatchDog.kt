package mods.eln.sim.process.destruct

import mods.eln.sim.mna.state.VoltageState

class VoltageStateWatchDog(
    var state: VoltageState,
    policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    randomFactor: WatchdogRandomFactor = WatchdogRandomFactor.LEGACY_UNIFORM,
) : ValueWatchdog(policy, randomFactor) {
    override val watchdogType = WatchdogType.VOLTAGE

    override fun getValue(): Double = state.voltage

    fun setNominalVoltage(nominalVoltage: Double): VoltageStateWatchDog = apply {
        max = nominalVoltage * 1.3
        min = -nominalVoltage * 1.3
        timeoutReset = nominalVoltage * 0.25
    }
}
