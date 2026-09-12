@file:Suppress("PropertyName")

package mods.eln.sim

enum class RegulatorType {
    None,
    Manual,
    OnOff,
    Analog,
}

abstract class RegulatorProcess(
    val name: String,
) : IProcess {
    var type: RegulatorType = RegulatorType.None
        private set
    var target: Double = 0.0
    var OnOffHysteresisDiv2: Double = 0.0
        private set
    var P: Double = 0.0
        private set
    var I: Double = 0.0
        private set
    var D: Double = 0.0
        private set
    var hitLast: Double = 0.0
        private set
    var errorIntegrated: Double = 0.0
        private set
    var boot: Boolean = true
        private set

    fun setManual() {
        type = RegulatorType.Manual
    }

    fun setNone() {
        type = RegulatorType.None
    }

    fun setOnOff(OnOffHysteresisFactor: Double, workingPoint: Double) {
        type = RegulatorType.OnOff
        OnOffHysteresisDiv2 = OnOffHysteresisFactor * workingPoint / 2.0
        boot = false
        setCmd(0.0)
    }

    fun setAnalog(P: Double, I: Double, D: Double, workingPoint: Double) {
        val normalizedP = P / workingPoint
        val normalizedI = I / workingPoint
        val normalizedD = D / workingPoint
        if (
            !boot &&
            (this.P != normalizedP || this.I != normalizedI || this.D != normalizedD || type != RegulatorType.Analog)
        ) {
            errorIntegrated = 0.0
            hitLast = getHit()
        }
        this.P = normalizedP
        this.I = normalizedI
        this.D = normalizedD
        type = RegulatorType.Analog
        boot = false
    }

    protected abstract fun getHit(): Double
    protected abstract fun setCmd(cmd: Double)

    override fun process(time: Double) {
        val hit = getHit()
        when (type) {
            RegulatorType.Manual -> Unit
            RegulatorType.None -> setCmd(1.0)
            RegulatorType.Analog -> {
                val error = target - hit
                val proportional = error * P
                var command = proportional - (hit - hitLast) * D * time
                errorIntegrated += error * time * I

                if (errorIntegrated > 1.0 - proportional) {
                    errorIntegrated = 1.0 - proportional
                    if (errorIntegrated < 0.0) errorIntegrated = 0.0
                } else if (errorIntegrated < -1.0 + proportional) {
                    errorIntegrated = -1.0 + proportional
                    if (errorIntegrated > 0.0) errorIntegrated = 0.0
                }

                command += errorIntegrated
                when {
                    command > 1.0 -> setCmd(1.0)
                    command < -1.0 -> setCmd(-1.0)
                    else -> setCmd(command)
                }
                hitLast = hit
            }
            RegulatorType.OnOff -> {
                if (hit > target + OnOffHysteresisDiv2) setCmd(0.0)
                if (hit < target - OnOffHysteresisDiv2) setCmd(1.0)
            }
        }
    }

    fun captureState(): RegulatorState = RegulatorState(errorIntegrated, target)

    fun restoreState(state: RegulatorState) {
        errorIntegrated = if (state.errorIntegrated.isNaN()) 0.0 else state.errorIntegrated
        target = state.target
    }
}

data class RegulatorState(
    val errorIntegrated: Double,
    val target: Double,
) {
    fun legacyErrorIntegratedKey(prefix: String, name: String): String = prefix + name + ERROR_INTEGRATED_SUFFIX
    fun legacyTargetKey(prefix: String, name: String): String = prefix + name + TARGET_SUFFIX

    companion object {
        const val ERROR_INTEGRATED_SUFFIX = "errorIntegrated"
        const val TARGET_SUFFIX = "target"
    }
}
