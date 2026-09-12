package mods.eln.misc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class FunctionTableParityTest {
    @Test
    fun `table interpolates and extrapolates linearly like legacy`() {
        val table = FunctionTable(doubleArrayOf(0.0, 10.0, 30.0), 2.0)

        assertEquals(-10.0, table.getValue(-1.0))
        assertEquals(5.0, table.getValue(0.5))
        assertEquals(20.0, table.getValue(1.5))
        assertEquals(50.0, table.getValue(3.0))
    }

    @Test
    fun `duplicate scales axes and owns a separate point array`() {
        val table = FunctionTable(doubleArrayOf(1.0, 3.0), 2.0)
        val duplicate = requireNotNull(table.duplicate(4.0, 5.0))

        assertEquals(8.0, duplicate.xMax)
        assertEquals(5.0, duplicate.point[0])
        assertEquals(15.0, duplicate.point[1])
        assertNotSame(table.point, duplicate.point)
    }

    @Test
    fun `protected table clamps values and duplicate bounds`() {
        val table = FunctionTableYProtect(doubleArrayOf(0.0, 10.0), 1.0, 2.0, 8.0)
        assertEquals(2.0, table.getValue(0.0))
        assertEquals(5.0, table.getValue(0.5))
        assertEquals(8.0, table.getValue(1.0))

        val duplicate = table.duplicate(2.0, 3.0) as FunctionTableYProtect
        assertEquals(6.0, duplicate.yMin)
        assertEquals(24.0, duplicate.yMax)
        assertEquals(2.0, duplicate.xMax)
    }

    @Test
    fun `changing xMax alone preserves cached inverse legacy quirk`() {
        val table = FunctionTable(doubleArrayOf(0.0, 10.0), 1.0)
        table.xMax = 2.0

        assertEquals(10.0, table.getValue(1.0))
        assertEquals(1.0, table.xMaxInv)
        assertEquals(1.0, table.xDelta)
    }
}
