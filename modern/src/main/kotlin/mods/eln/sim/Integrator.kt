package mods.eln.sim

class Integrator {
    private var integrationPhase = 0
    private var stepsTaken = 0
    private var sample = DoubleArray(5)
    private var integrations = DoubleArray(4)

    init {
        reset()
    }

    fun reset() {
        stepsTaken = 0
        integrationPhase = 0
        sample = DoubleArray(5)
        integrations = DoubleArray(4)
    }

    fun nextStep(nextValue: Double, timeStep: Double): Double {
        var summation = 0.0
        stepsTaken++
        summation += sample[1].also { sample[0] = it } * 7.0
        summation += sample[2].also { sample[1] = it } * 32.0
        summation += sample[3].also { sample[2] = it } * 12.0
        summation += sample[4].also { sample[3] = it } * 32.0
        summation += nextValue.also { sample[4] = it } * 7.0
        summation *= 2.0 * timeStep / 45.0
        summation = summation.let { integrations[integrationPhase] += it; integrations[integrationPhase] }
        integrationPhase = integrationPhase + 1 and 3
        return summation
    }
}
