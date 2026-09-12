package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TimeRemoverParityTest {
    @Test
    fun `arming registers once and expiry removes process`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        var additions = 0
        var removals = 0
        val observer = object : ITimeRemoverObserver {
            override fun timeRemoverRemove() { removals++ }
            override fun timeRemoverAdd() { additions++ }
        }
        val remover = TimeRemover(observer, simulator)

        remover.setTimeout(0.1)
        remover.setTimeout(0.1)
        assertEquals(1, additions)
        assertTrue(remover.isArmed)

        repeat(2) { simulator.tick() }
        assertEquals(1, removals)
        assertFalse(remover.isArmed)

        simulator.tick()
        assertEquals(1, removals)
    }

    @Test
    fun `non positive timeout still performs legacy registration callback`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        var additions = 0
        val observer = object : ITimeRemoverObserver {
            override fun timeRemoverRemove() = Unit
            override fun timeRemoverAdd() { additions++ }
        }
        val remover = TimeRemover(observer, simulator)

        remover.setTimeout(0.0)

        assertEquals(1, additions)
        assertFalse(remover.isArmed)
    }

    @Test
    fun `manual shot notifies even while disarmed`() {
        val simulator = Simulator(0.05, 0.05, 1, 0.05)
        var removals = 0
        val remover = TimeRemover(object : ITimeRemoverObserver {
            override fun timeRemoverRemove() { removals++ }
            override fun timeRemoverAdd() = Unit
        }, simulator)

        remover.shot()

        assertEquals(1, removals)
        assertEquals(0.0, remover.timeout)
    }
}
