package mods.eln.sim

import mods.eln.misc.FunctionTable
import mods.eln.sim.mna.component.VoltageSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BatteryProcessParityTest {
    private fun battery(
        curve: FunctionTable = FunctionTable(doubleArrayOf(1.0, 1.0), 1.0),
        thermal: ThermalLoad = ThermalLoad(),
    ): BatteryProcess = BatteryProcess(null, null, curve, 10.0, VoltageSource("battery"), thermal).apply {
        QNominal = 10.0
        uNominal = 12.0
        Q = 0.5
    }

    @Test
    fun `discharge updates charge and source voltage`() {
        val battery = battery()
        battery.voltageSource.currentState.state = -2.0

        battery.process(1.0)

        assertEquals(0.3, battery.Q)
        assertEquals(12.0, battery.voltageSource.voltage)
        assertEquals(2.0, battery.dischargeCurrent)
    }

    @Test
    fun `non rechargeable battery holds charge and converts recharge to heat`() {
        val thermal = ThermalLoad()
        val battery = battery(thermal = thermal).apply { isRechargeable = false }
        battery.voltageSource.currentState.state = 3.0

        battery.process(1.0)

        assertEquals(0.5, battery.Q)
        assertEquals(36.0, thermal.PcTemp)
        assertEquals(36.0, thermal.PspTemp)
    }

    @Test
    fun `rechargeable battery has no legacy upper charge clamp`() {
        val battery = battery().apply { Q = 0.9 }
        battery.voltageSource.currentState.state = 3.0

        battery.process(1.0)

        assertEquals(1.2, battery.Q)
    }

    @Test
    fun `life reduction scales Q while life increase does not`() {
        val battery = battery().apply { Q = 1.0; life = 1.0 }

        battery.changeLife(0.5)
        assertEquals(0.5, battery.Q)
        assertEquals(0.5, battery.life)

        battery.changeLife(1.0)
        assertEquals(0.5, battery.Q)
        assertEquals(1.0, battery.life)
    }

    @Test
    fun `energy integration retains fifty sample legacy result`() {
        val battery = battery().apply {
            QNominal = 2.0
            uNominal = 4.0
            life = 0.5
            Q = 0.5
        }

        assertEquals(4.0, battery.energy, 1.0e-12)
        assertEquals(4.0, battery.energyMax, 1.0e-12)
        assertEquals(1.0, battery.charge)
    }

    @Test
    fun `state restore repairs non finite battery values`() {
        val battery = battery()

        battery.restoreState(BatteryState(Double.NaN, Double.POSITIVE_INFINITY))

        assertEquals(0.0, battery.Q)
        assertEquals(1.0, battery.life)
        assertEquals("NBPQ", BatteryState.LEGACY_Q_SUFFIX)
        assertEquals("NBPlife", BatteryState.LEGACY_LIFE_SUFFIX)
    }
}
