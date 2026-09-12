package mods.eln.misc

class FunctionTableYProtect(
    point: DoubleArray,
    xMax: Double,
    var yMin: Double,
    var yMax: Double,
) : FunctionTable(point, xMax) {
    override fun getValue(x: Double): Double {
        val value = super.getValue(x)
        if (value > yMax) return yMax
        return if (value < yMin) yMin else value
    }

    override fun duplicate(xFactor: Double, yFactor: Double): FunctionTable {
        val copy = DoubleArray(point.size)
        for (index in point.indices) copy[index] = point[index] * yFactor
        return FunctionTableYProtect(copy, xMax * xFactor, yMin * yFactor, yMax * yFactor)
    }
}
