package mods.eln.misc

open class FunctionTable(
    var point: DoubleArray,
    var xMax: Double,
) : IFunction {
    open var xMaxInv: Double = 1.0 / xMax
    open var xDelta: Double = 1.0 / (point.size - 1) * xMax

    override fun getValue(x: Double): Double {
        var localX = x * xMaxInv
        if (localX < 0.0f) {
            return point[0] + (point[1] - point[0]) * (point.size - 1) * localX
        }
        if (localX >= 1.0f) {
            return point[point.size - 1] +
                (point[point.size - 1] - point[point.size - 2]) * (point.size - 1) * (localX - 1.0)
        }
        localX *= (point.size - 1).toDouble()
        val index = localX.toInt()
        localX -= index.toDouble()
        return point[index + 1] * localX + point[index] * (1.0f - localX)
    }

    open fun duplicate(xFactor: Double, yFactor: Double): FunctionTable? {
        val copy = DoubleArray(point.size)
        for (index in point.indices) copy[index] = point[index] * yFactor
        return FunctionTable(copy, xMax * xFactor)
    }
}
