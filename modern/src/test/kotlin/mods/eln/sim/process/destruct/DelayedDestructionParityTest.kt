package mods.eln.sim.process.destruct

import mods.eln.sim.Simulator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DelayedDestructionParityTest {
    @Test
    fun `process registers itself and destroys once after timeout`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        val target = WatchdogStubDestructible()
        val delayed = DelayedDestruction(target, 0.1, simulator)

        simulator.tick()
        assertEquals(0.05, delayed.timeout)
        assertEquals(0, target.destructionCount)

        simulator.tick()
        assertEquals(0.0, delayed.timeout)
        assertEquals(1, target.destructionCount)

        simulator.tick()
        assertEquals(1, target.destructionCount)
    }
}
