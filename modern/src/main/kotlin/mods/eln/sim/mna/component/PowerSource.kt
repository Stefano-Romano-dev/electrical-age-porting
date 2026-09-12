package mods.eln.sim.mna.component

import kotlin.math.sqrt
import mods.eln.sim.mna.SubSystem
import mods.eln.sim.mna.misc.IRootSystemPreStepProcess
import mods.eln.sim.mna.state.State

class PowerSource(
    name: String,
    aPin: State,
) : VoltageSource(name, aPin, null), IRootSystemPreStepProcess {
    private var requestedPower = 0.0

    override val power: Double
        get() = requestedPower

    var maximumVoltage: Double = 0.0
        private set

    var maximumCurrent: Double = 0.0
        private set

    val effectivePower: Double
        get() = voltage * current

    fun setPower(power: Double) {
        requestedPower = power
    }

    fun setMaximums(maximumVoltage: Double, maximumCurrent: Double) {
        this.maximumVoltage = maximumVoltage
        this.maximumCurrent = maximumCurrent
    }

    fun setMaximumVoltage(maximumVoltage: Double) {
        this.maximumVoltage = maximumVoltage
    }

    fun setMaximumCurrent(maximumCurrent: Double) {
        this.maximumCurrent = maximumCurrent
    }

    override fun addToSubsystem(subSystem: SubSystem) {
        super.addToSubsystem(subSystem)
        requireNotNull(subSystem.root) { "PowerSource requires a root-owned subsystem" }.addProcess(this)
    }

    override fun quitSubSystem() {
        directSubSystem?.root?.removeProcess(this)
        super.quitSubSystem()
    }

    override fun rootSystemPreStepProcess() {
        val state = requireNotNull(aPin)
        val equivalent = requireNotNull(state.subSystem).getTh(state, this)
        var target = (sqrt(equivalent.voltage * equivalent.voltage + 4.0 * power * equivalent.resistance) +
            equivalent.voltage) / 2.0
        target = minOf(target, maximumVoltage, equivalent.voltage + equivalent.resistance * maximumCurrent)
        if (target.isNaN()) target = 0.0
        if (target < equivalent.voltage) target = equivalent.voltage
        setVoltage(target)
    }
}
