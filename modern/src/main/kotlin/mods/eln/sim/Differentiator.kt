package mods.eln.sim

class Differentiator {
    private var stepsTaken = 0
    private var sample = doubleArrayOf(0.0, 0.0, 0.0, 0.0)

    fun reset() {
        // Legacy intentionally leaves stepsTaken unchanged.
        sample = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
    }

    fun nextStep(nextValue: Double, timeStep: Double): Double {
        var summation = 0.0
        if (stepsTaken >= 4) {
            summation += sample[1].also { sample[0] = it } * -2.0
            summation += sample[2].also { sample[1] = it } * 9.0
            summation += sample[3].also { sample[2] = it } * -18.0
            summation += nextValue.also { sample[3] = it } * 11.0
            summation /= 6.0 * timeStep
        } else {
            sample[0] = sample[1]
            sample[1] = sample[2]
            summation += sample[3].also { sample[2] = it } * -1.0
            summation += nextValue.also { sample[3] = it }
            summation /= timeStep
        }
        stepsTaken++
        return summation
    }
}
