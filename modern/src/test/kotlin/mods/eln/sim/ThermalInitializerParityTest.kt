package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ThermalInitializerParityTest {
    private val validator = Simulator(0.05, 0.05, 1, 0.0025)

    @Test
    fun `tao initializer retains legacy formulas and applies values`() {
        val initializer = ThermalLoadInitializer(80.0, -100.0, 10.0, 100_000.0, validator)
        initializer.setMaximalPower(100.0)
        val load = ThermalLoad()
        initializer.applyTo(load)

        assertEquals(12.5, initializer.C)
        assertEquals(0.8, initializer.Rp)
        assertEquals(4_000.0, initializer.Rs)
        assertEquals(initializer.C, load.heatCapacity)
        assertEquals(initializer.Rp, load.Rp)
        assertEquals(initializer.Rs, load.Rs)
    }

    @Test
    fun `power drop initializer retains legacy formulas`() {
        val initializer = ThermalLoadInitializerByPowerDrop(780.0, -100.0, 10.0, 2.0, validator)
        initializer.setMaximalPower(2_000.0)
        val load = ThermalLoad()
        initializer.applyToThermalLoad(load)

        assertEquals(2_000.0 * 10.0 / 780.0, initializer.C)
        assertEquals(780.0 / 2_000.0, initializer.Rp)
        assertEquals(2.0 / 2_000.0 / 2.0, initializer.Rs)
        assertEquals(initializer.C, load.heatCapacity)
        assertEquals(initializer.Rp, load.Rp)
        assertEquals(initializer.Rs, load.Rs)
    }

    @Test
    fun `copies retain computed parameters independently`() {
        val initializer = ThermalLoadInitializer(80.0, -20.0, 10.0, 100.0, validator)
        initializer.setMaximalPower(40.0)

        val copy = initializer.copy()
        initializer.setMaximalPower(80.0)

        assertEquals(5.0, copy.C)
        assertEquals(2.0, copy.Rp)
        assertEquals(10.0, copy.Rs)
        assertEquals(-20.0, copy.minimumTemperature)
    }

    @Test
    fun `initializer delegates legacy stability rejection to simulator owner`() {
        val strictValidator = Simulator(0.05, 0.05, 1, 0.05)
        val initializer = ThermalLoadInitializer(80.0, -20.0, 0.0001, 100.0, strictValidator)

        assertThrows(IllegalStateException::class.java) {
            initializer.setMaximalPower(100.0)
        }
    }
}
