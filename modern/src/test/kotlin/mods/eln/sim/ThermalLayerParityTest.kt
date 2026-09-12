package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThermalLayerParityTest {
    @Test
    fun `default and explicit constructors retain legacy values`() {
        val default = ThermalLoad()
        assertEquals(0.0, default.temperatureCelsius)
        assertEquals(1.0e9, default.Rs)
        assertEquals(1.0e9, default.Rp)
        assertEquals(1.0, default.heatCapacity)

        val explicit = ThermalLoad(25.0, 4.0, 3.0, 2.0)
        assertEquals(25.0, explicit.temperatureCelsius)
        assertEquals(4.0, explicit.Rp)
        assertEquals(3.0, explicit.Rs)
        assertEquals(2.0, explicit.heatCapacity)
    }

    @Test
    fun `energy and power movement preserve signs and absolute throughput`() {
        val from = ThermalLoad()
        val to = ThermalLoad()

        ThermalLoad.moveEnergy(12.0, 3.0, from, to)
        assertEquals(-4.0, from.PcTemp)
        assertEquals(4.0, to.PcTemp)
        assertEquals(4.0, from.PspTemp)
        assertEquals(4.0, to.PspTemp)

        ThermalLoad.movePower(-2.0, from, to)
        assertEquals(-2.0, from.PcTemp)
        assertEquals(2.0, to.PcTemp)
        assertEquals(6.0, from.PspTemp)
        assertEquals(6.0, to.PspTemp)
    }

    @Test
    fun `direct power keeps legacy signed throughput quirk`() {
        val load = ThermalLoad()

        load.movePowerTo(-3.0)

        assertEquals(-3.0, load.PcTemp)
        assertEquals(-3.0, load.PspTemp)
    }

    @Test
    fun `invalid source accumulator prevents legacy transfer`() {
        val from = ThermalLoad().apply { PcTemp = Double.NaN }
        val to = ThermalLoad()

        ThermalLoad.movePower(5.0, from, to)

        assertEquals(0.0, to.PcTemp)
        assertEquals(0.0, to.PspTemp)
    }

    @Test
    fun `temperature getter repairs only NaN like legacy`() {
        val load = ThermalLoad().apply { temperatureCelsius = Double.NaN }
        assertEquals(0.0, load.getTemperature())

        load.temperatureCelsius = Double.POSITIVE_INFINITY
        assertEquals(Double.POSITIVE_INFINITY, load.getTemperature())
    }

    @Test
    fun `thermal resistor transfers instantaneous power`() {
        val hot = ThermalLoad(80.0, 10.0, 2.0, 5.0)
        val cold = ThermalLoad(20.0, 10.0, 2.0, 5.0)
        val resistor = ThermalResistor(hot, cold).apply { setThermalResistance(3.0) }

        assertEquals(20.0, resistor.power)
        resistor.process(123.0)

        assertEquals(-20.0, hot.PcTemp)
        assertEquals(20.0, cold.PcTemp)
    }

    @Test
    fun `slow flag and simulation coordinates round trip`() {
        val load = ThermalLoad()
        assertFalse(load.isSlow)
        assertFalse(load.hasSimCoordinate())

        load.setAsSlow()
        load.setSimCoordinate(7, 1, 2, 3)
        assertTrue(load.isSlow)
        assertTrue(load.hasSimCoordinate())
        assertEquals(7, load.getSimDimension())
        assertEquals(1, load.getSimX())
        assertEquals(2, load.getSimY())
        assertEquals(3, load.getSimZ())

        load.setAsFast()
        load.clearSimCoordinate()
        assertFalse(load.isSlow)
        assertFalse(load.hasSimCoordinate())
    }
}
