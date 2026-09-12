package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ThermalSimulatorParityTest {
    @Test
    fun `step follows legacy connection process ambient and integration order`() {
        val hot = ThermalLoad(100.0, 100.0, 2.0, 10.0)
        val cold = ThermalLoad(20.0, 100.0, 3.0, 20.0)
        var powerSeenByProcess = Double.NaN
        val process = IProcess {
            powerSeenByProcess = hot.PcTemp
            hot.movePowerTo(6.0)
        }

        ThermalSimulator().step(
            time = 0.5,
            connections = listOf(ThermalConnection(hot, cold)),
            processes = listOf(process),
            loads = listOf(hot, cold),
        )

        assertEquals(-16.0, powerSeenByProcess)
        assertEquals(99.45, hot.temperatureCelsius, 1.0e-12)
        assertEquals(20.395, cold.temperatureCelsius, 1.0e-12)
        assertEquals(-11.0, hot.Pc)
        assertEquals(15.8, cold.Pc)
        assertEquals(16.0, hot.Prs)
        assertEquals(16.0, cold.Prs)
        assertEquals(6.0, hot.Psp)
        assertEquals(0.0, hot.PcTemp)
        assertEquals(0.0, hot.PrsTemp)
        assertEquals(0.0, hot.PspTemp)
    }

    @Test
    fun `coordinate load delegates ambient exchange through platform boundary`() {
        val load = ThermalLoad(50.0, 10.0, 4.0, 20.0).apply {
            setSimCoordinate(2, 10, 20, 30)
            PcTemp = 10.0
        }
        var calls = 0
        val ambient = ThermalAmbientExchange { received, time ->
            calls++
            assertEquals(load, received)
            assertEquals(2.0, time)
            4.0
        }

        ThermalSimulator(ambient).step(2.0, emptyList(), emptyList(), listOf(load))

        assertEquals(1, calls)
        assertEquals(50.6, load.temperatureCelsius)
        assertEquals(6.0, load.Pc)
    }

    @Test
    fun `ambient adapter is ignored without a simulation coordinate`() {
        val load = ThermalLoad(50.0, 10.0, 4.0, 20.0)
        var calls = 0
        val ambient = ThermalAmbientExchange { _, _ -> calls++; 4.0 }

        ThermalSimulator(ambient).step(2.0, emptyList(), emptyList(), listOf(load))

        assertEquals(0, calls)
        assertEquals(49.5, load.temperatureCelsius)
        assertEquals(-5.0, load.Pc)
    }
}
