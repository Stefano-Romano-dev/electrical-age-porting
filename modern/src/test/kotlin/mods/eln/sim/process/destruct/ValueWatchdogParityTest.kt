package mods.eln.sim.process.destruct

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class WatchdogStubDestructible : IDestructible {
    var destructionCount = 0

    override fun destructImpl() {
        destructionCount++
    }

    override fun describe(): String = "stub"
}

private class TestWatchdog(
    var watchedValue: Double,
    policy: WatchdogPolicy = WatchdogPolicy.ENABLED,
    randomFactor: WatchdogRandomFactor = WatchdogRandomFactor { 1.0 },
) : ValueWatchdog(policy, randomFactor) {
    var tripCount = 0
    var lastTripValue = 0.0
    var lastOverflow = 0.0

    override fun getValue(): Double = watchedValue

    override fun onDestroy(value: Double, overflow: Double) {
        tripCount++
        lastTripValue = value
        lastOverflow = overflow
    }
}

class ValueWatchdogParityTest {
    @Test
    fun `first consecutive overflow is forgiven then destruction triggers`() {
        val target = WatchdogStubDestructible()
        val watchdog = TestWatchdog(10.0).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 0.0
            setDestroys(target)
        }

        watchdog.process(1.0)
        assertEquals(0, watchdog.tripCount)
        assertEquals(0, target.destructionCount)

        watchdog.process(1.0)
        assertEquals(1, watchdog.tripCount)
        assertEquals(1, target.destructionCount)
        assertEquals(10.0, watchdog.lastTripValue)
        assertEquals(9.0, watchdog.lastOverflow)

        watchdog.process(1.0)
        assertEquals(2, watchdog.tripCount)
        assertEquals(2, target.destructionCount)
    }

    @Test
    fun `disabled category still reports trip but does not destruct`() {
        val target = WatchdogStubDestructible()
        val watchdog = TestWatchdog(10.0, WatchdogPolicy.DISABLED).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 0.0
            setDestroys(target)
        }

        watchdog.process(1.0)
        watchdog.process(1.0)

        assertEquals(1, watchdog.tripCount)
        assertEquals(0, target.destructionCount)
    }

    @Test
    fun `safe value recharges timeout and restores overflow forgiveness`() {
        val watchdog = TestWatchdog(10.0).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 10.0
        }
        watchdog.process(1.0)
        watchdog.process(1.0)
        assertEquals(1.0, watchdog.timeout)

        watchdog.watchedValue = 0.0
        watchdog.process(1.0)
        assertEquals(2.0, watchdog.timeout)

        watchdog.watchedValue = 10.0
        watchdog.process(1.0)
        assertEquals(2.0, watchdog.timeout)
    }

    @Test
    fun `reset retains legacy joker state`() {
        val target = WatchdogStubDestructible()
        val watchdog = TestWatchdog(10.0).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 0.0
            setDestroys(target)
        }
        watchdog.process(1.0)

        watchdog.reset()
        assertTrue(watchdog.boot)
        watchdog.process(1.0)

        assertFalse(watchdog.boot)
        assertEquals(1, target.destructionCount)
    }

    @Test
    fun `random factor scales timeout depletion`() {
        val watchdog = TestWatchdog(5.0, randomFactor = WatchdogRandomFactor { 0.5 }).apply {
            min = -1.0
            max = 1.0
            timeoutReset = 10.0
        }

        watchdog.process(1.0)
        watchdog.process(2.0)

        assertEquals(6.0, watchdog.timeout)
    }
}
