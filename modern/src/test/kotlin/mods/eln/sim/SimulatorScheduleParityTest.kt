package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimulatorScheduleParityTest {
    @Test
    fun `default periods retain extra electrical step on first tick`() {
        val simulator = Simulator(0.05, 0.05, 50, 1.0 / 400.0)
        var electricalCalls = 0
        var thermalCalls = 0
        var slowThermalCalls = 0
        simulator.addElectricalProcess(IProcess { time ->
            assertEquals(0.05, time)
            electricalCalls++
        })
        simulator.addThermalFastProcess(IProcess { time ->
            assertEquals(0.0025, time)
            thermalCalls++
        })
        simulator.addThermalSlowProcess(IProcess { time ->
            assertEquals(0.05, time)
            slowThermalCalls++
        })

        simulator.tick()

        assertEquals(2, electricalCalls)
        assertEquals(20, thermalCalls)
        assertEquals(1, slowThermalCalls)

        simulator.tick()
        assertEquals(3, electricalCalls)
        assertEquals(40, thermalCalls)
        assertEquals(2, slowThermalCalls)
    }

    @Test
    fun `outer phases retain legacy ordering`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        val phases = mutableListOf<String>()
        simulator.addSlowPreProcess(IProcess { phases += "pre" })
        simulator.addElectricalProcess(IProcess { phases += "electrical" })
        simulator.addThermalFastProcess(IProcess { phases += "thermal-fast" })
        simulator.addThermalSlowProcess(IProcess { phases += "thermal-slow" })
        simulator.addSlowProcess(IProcess { phases += "slow" })
        simulator.queueDestruction { phases += "destruction" }
        simulator.addSlowPostProcess(IProcess { phases += "post" })

        simulator.tick()

        assertEquals(
            listOf(
                "pre",
                "electrical",
                "thermal-fast",
                "electrical",
                "thermal-slow",
                "slow",
                "destruction",
                "post",
            ),
            phases,
        )
    }

    @Test
    fun `slow process snapshot tolerates self removal like legacy toArray`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        var calls = 0
        lateinit var process: IProcess
        process = IProcess {
            calls++
            simulator.removeSlowProcess(process)
        }
        simulator.addSlowProcess(process)

        simulator.tick()
        simulator.tick()

        assertEquals(1, calls)
    }

    @Test
    fun `thermal connections reject mixed fast and slow loads`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        val fast = ThermalLoad()
        val slow = ThermalLoad().apply { setAsSlow() }

        assertFalse(simulator.addThermalConnection(ThermalConnection(fast, slow)))
        assertTrue(simulator.addThermalConnection(ThermalConnection(fast, ThermalLoad())))
        assertTrue(simulator.addThermalConnection(ThermalConnection(slow, ThermalLoad().apply { setAsSlow() })))
    }

    @Test
    fun `thermal stability check uses legacy equivalent resistance formula`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.0025)
        val expected = 0.0025 * 3.0 / (1.0 / (1.0 / 4.0 + 1.0 / 12.0))

        assertEquals(expected, simulator.getMinimalThermalC(4.0, 12.0), 1.0e-15)
        assertTrue(simulator.checkThermalLoad(4.0, 12.0, expected))
        assertThrows(IllegalStateException::class.java) {
            simulator.checkThermalLoad(4.0, 12.0, Math.nextDown(expected))
        }
    }
}
