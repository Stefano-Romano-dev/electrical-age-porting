package mods.eln.sim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NumericalUtilityParityTest {
    @Test
    fun `differentiator startup uses first order difference`() {
        val differentiator = Differentiator()
        val actual = (1..4).map { differentiator.nextStep(it.toDouble(), 0.5) }

        actual.forEach { assertEquals(2.0, it, 1.0e-12) }
    }

    @Test
    fun `differentiator higher order stencil tracks linear slope`() {
        val differentiator = Differentiator()
        var last = 0.0
        for (step in 0..8) last = differentiator.nextStep(3.5 * step * 0.1, 0.1)

        assertEquals(3.5, last, 1.0e-12)
    }

    @Test
    fun `differentiator reset preserves legacy warmup counter`() {
        val differentiator = Differentiator()
        repeat(8) { differentiator.nextStep(it * 0.25, 0.25) }

        differentiator.reset()

        assertEquals(22.0 / (6.0 * 0.25), differentiator.nextStep(2.0, 0.25), 1.0e-12)
    }

    @Test
    fun `integrator constant input retains round robin sequence`() {
        val integrator = Integrator()
        val actual = List(6) { integrator.nextStep(1.0, 1.0) }
        val expected = listOf(14.0, 78.0, 102.0, 166.0, 194.0, 258.0).map { it / 45.0 }

        expected.zip(actual).forEach { (expectedValue, actualValue) ->
            assertEquals(expectedValue, actualValue, 1.0e-12)
        }
    }

    @Test
    fun `integrator reset clears phases samples and totals`() {
        val integrator = Integrator()
        repeat(10) { integrator.nextStep(2.0, 0.25) }

        integrator.reset()

        assertEquals(14.0 / 45.0 * 2.0 * 0.25, integrator.nextStep(2.0, 0.25), 1.0e-12)
    }
}
