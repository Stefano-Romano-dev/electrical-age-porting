package mods.eln.sim

import mods.eln.misc.FunctionTable
import mods.eln.sim.mna.component.VoltageSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class TestBatterySlowProcess(
    battery: BatteryProcess,
    agingPolicy: BatteryAgingPolicy = BatteryAgingPolicy.ENABLED,
) : BatterySlowProcess(battery, ThermalLoad(), agingPolicy) {
    var destroyed = false

    override fun destroy() {
        destroyed = true
    }
}

class BatterySlowProcessParityTest {
    private fun battery(curveValue: Double = 1.0): BatteryProcess = BatteryProcess(
        null,
        null,
        FunctionTable(doubleArrayOf(curveValue, curveValue), 1.0),
        10.0,
        VoltageSource("battery"),
        ThermalLoad(),
    ).apply {
        QNominal = 1.0
        uNominal = 10.0
        Q = 1.0
    }

    @Test
    fun `invalid negative nominal voltage destroys battery`() {
        val battery = battery().apply { uNominal = -10.0 }
        val slow = TestBatterySlowProcess(battery)

        slow.process(1.0)

        assertTrue(slow.destroyed)
    }

    @Test
    fun `overvoltage destroys battery`() {
        val slow = TestBatterySlowProcess(battery(curveValue = 2.0))

        slow.process(1.0)

        assertTrue(slow.destroyed)
    }

    @Test
    fun `enabled aging reduces life quadratically and scales charge`() {
        val battery = battery()
        battery.voltageSource.currentState.state = -1.0
        val slow = TestBatterySlowProcess(battery).apply {
            lifeNominalCurrent = 1.0
            lifeNominalLost = 0.5
        }

        slow.process(1.0)

        assertFalse(slow.destroyed)
        assertEquals(0.5, battery.life)
        assertEquals(0.5, battery.Q)
    }

    @Test
    fun `disabled aging policy leaves life unchanged`() {
        val battery = battery()
        battery.voltageSource.currentState.state = -10.0
        val slow = TestBatterySlowProcess(battery, BatteryAgingPolicy.DISABLED).apply {
            lifeNominalCurrent = 1.0
            lifeNominalLost = 1.0
        }

        slow.process(10.0)

        assertEquals(1.0, battery.life)
        assertEquals(1.0, battery.Q)
    }

    @Test
    fun `aging lower bound remains ten percent`() {
        val battery = battery()
        battery.voltageSource.currentState.state = -100.0
        val slow = TestBatterySlowProcess(battery).apply {
            lifeNominalCurrent = 1.0
            lifeNominalLost = 100.0
        }

        slow.process(1.0)

        assertEquals(0.1, battery.life)
        assertEquals(0.1, battery.Q)
    }
}
