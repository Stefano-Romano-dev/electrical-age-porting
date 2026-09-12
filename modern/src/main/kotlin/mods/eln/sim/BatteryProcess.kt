@file:Suppress("PropertyName")

package mods.eln.sim

import mods.eln.misc.FunctionTable
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.state.VoltageState
import kotlin.math.abs
import kotlin.math.max

open class BatteryProcess(
    var positiveLoad: VoltageState?,
    var negativeLoad: VoltageState?,
    var voltageFunction: FunctionTable,
    @JvmField var IMax: Double,
    var voltageSource: VoltageSource,
    private var thermalLoad: ThermalLoad,
) : IProcess {
    @JvmField
    var Q: Double = 0.0
    var QNominal: Double = 0.0
    var uNominal: Double = 0.0

    @JvmField
    var life: Double = 1.0
    var isRechargeable: Boolean = true

    override fun process(time: Double) {
        val lastQ = Q
        var wasteQ = 0.0
        val deltaQ = voltageSource.current * time / QNominal
        if (!isRechargeable && deltaQ < 0.0) {
            wasteQ = -deltaQ
            Q = lastQ
        } else {
            Q = max(Q - deltaQ, 0.0)
        }
        val voltage = computeVoltage()
        voltageSource.setVoltage(voltage)
        if (wasteQ > 0.0) {
            thermalLoad.movePowerTo(abs(voltageSource.current * voltage))
        }
    }

    fun computeVoltage(): Double = max(0.0, voltageFunction.getValue(Q / life) * uNominal)

    fun changeLife(newLife: Double) {
        if (newLife < life) Q *= newLife / life
        life = newLife
    }

    var charge: Double
        get() = Q / life
        set(charge) {
            Q = life * charge
        }

    val energy: Double
        get() {
            val stepCount = 50
            val chargeStep = charge / stepCount
            var chargeIntegrator = 0.0
            var result = 0.0
            val chargePerStep = QNominal * life * chargeStep
            for (step in 0 until stepCount) {
                val voltage = voltageFunction.getValue(chargeIntegrator) * uNominal
                result += voltage * chargePerStep
                chargeIntegrator += chargeStep
            }
            return result
        }

    val energyMax: Double
        get() {
            val stepCount = 50
            val chargeStep = 1.0 / stepCount
            var chargeIntegrator = 0.0
            var result = 0.0
            val chargePerStep = QNominal * life / stepCount
            for (step in 0 until stepCount) {
                val voltage = voltageFunction.getValue(chargeIntegrator) * uNominal
                result += voltage * chargePerStep
                chargeIntegrator += chargeStep
            }
            return result
        }

    val u: Double
        get() = computeVoltage()

    val dischargeCurrent: Double
        get() = voltageSource.current

    fun captureState(): BatteryState = BatteryState(Q, life)

    fun restoreState(state: BatteryState) {
        Q = if (state.q.isFinite()) state.q else 0.0
        life = if (state.life.isFinite()) state.life else 1.0
    }
}

data class BatteryState(
    val q: Double,
    val life: Double,
) {
    companion object {
        const val LEGACY_Q_SUFFIX = "NBPQ"
        const val LEGACY_LIFE_SUFFIX = "NBPlife"
    }
}
