package mods.eln.sim.process.destruct

import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.state.VoltageState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ElectricalWatchdogParityTest {
    @Test
    fun `voltage state nominal values configure legacy limits`() {
        val state = VoltageState().apply { voltage = 5.0 }
        val watchdog = VoltageStateWatchDog(state).setNominalVoltage(10.0)

        assertEquals(13.0, watchdog.max)
        assertEquals(-13.0, watchdog.min)
        assertEquals(2.5, watchdog.timeoutReset)
        assertEquals(5.0, watchdog.getValue())
    }

    @Test
    fun `bipole voltage watchdog uses double nominal timeout`() {
        val a = VoltageState().apply { voltage = 4.0 }
        val b = VoltageState().apply { voltage = 1.0 }
        val watchdog = BipoleVoltageWatchdog(Resistor(a, b)).setNominalVoltage(10.0)

        assertEquals(13.0, watchdog.max)
        assertEquals(-13.0, watchdog.min)
        assertEquals(5.0, watchdog.timeoutReset)
        assertEquals(3.0, watchdog.getValue())
    }

    @Test
    fun `resistor power watchdog reads dissipated power`() {
        val a = VoltageState().apply { voltage = 6.0 }
        val resistor = Resistor(a, null).apply { setResistance(3.0) }
        val watchdog = ResistorPowerWatchdog(resistor).setMaximumPower(10.0)

        assertEquals(10.0, watchdog.max)
        assertEquals(-1.0, watchdog.min)
        assertEquals(10.0, watchdog.timeoutReset)
        assertEquals(12.0, watchdog.getValue())
    }

    @Test
    fun `watchdog policy receives the concrete category`() {
        val categories = mutableListOf<WatchdogType>()
        val target = object : IDestructible {
            override fun destructImpl() = Unit
            override fun describe(): String = "target"
        }
        val state = VoltageState().apply { voltage = 10.0 }
        val watchdog = VoltageStateWatchDog(
            state,
            WatchdogPolicy { categories += it; false },
            WatchdogRandomFactor { 1.0 },
        ).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 0.0
            setDestroys(target)
        }

        watchdog.process(1.0)
        watchdog.process(1.0)

        assertEquals(listOf(WatchdogType.VOLTAGE), categories)
    }
}
