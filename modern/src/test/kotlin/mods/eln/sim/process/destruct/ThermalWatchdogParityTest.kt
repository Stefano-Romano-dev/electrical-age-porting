package mods.eln.sim.process.destruct

import mods.eln.sim.Simulator
import mods.eln.sim.ThermalLoad
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ThermalWatchdogParityTest {
    private val validator = Simulator(0.05, 0.05, 1, 0.0025)

    @Test
    fun `all limit setters retain legacy values`() {
        val watchdog = ThermalLoadWatchDog(ThermalLoad())
        watchdog.setMaximumTemperature(100.0)
        assertEquals(100.0, watchdog.max)
        assertEquals(-40.0, watchdog.min)
        assertEquals(100.0, watchdog.timeoutReset)

        val initializer = ThermalLoadInitializer(120.0, -20.0, 1.0, 1.0, validator)
        watchdog.setThermalLoad(initializer)
        assertEquals(120.0, watchdog.max)
        assertEquals(-20.0, watchdog.min)

        watchdog.setTemperatureLimits(90.0, -10.0)
        assertEquals(90.0, watchdog.max)
        assertEquals(-10.0, watchdog.min)

        val byDrop = ThermalLoadInitializerByPowerDrop(80.0, -5.0, 1.0, 1.0, validator)
        watchdog.setTemperatureLimits(byDrop)
        assertEquals(80.0, watchdog.max)
        assertEquals(-5.0, watchdog.min)
    }

    @Test
    fun `ambient provider contributes to absolute watched temperature`() {
        val watchdog = ThermalLoadWatchDog(ThermalLoad().apply { temperatureCelsius = 40.0 })
            .setAmbientTemperatureProvider { 25.0 }

        assertEquals(65.0, watchdog.getValue())
    }

    @Test
    fun `trip reports thermal trajectory and requests matrix dump`() {
        val load = ThermalLoad(10.0, 1.0, 1.0, 4.0).apply { Pc = 7.0 }
        val dumpTarget = Any()
        var dumpedTarget: Any? = null
        var dumpReason: String? = null
        var trip: ThermalWatchdogTrip? = null
        val watchdog = ThermalLoadWatchDog(
            load,
            randomFactor = WatchdogRandomFactor { 1.0 },
            diagnosticSink = WatchdogDiagnosticSink { target, reason ->
                dumpedTarget = target
                dumpReason = reason
            },
            tripObserver = { trip = it },
        ).setAmbientTemperatureProvider { 3.0 }
            .dumpMatrixOnTrip { dumpTarget }
            .setTemperatureLimits(1.0, -1.0)
        watchdog.timeoutReset = 0.0

        load.temperatureCelsius = 20.0
        watchdog.process(2.0)
        load.temperatureCelsius = 30.0
        watchdog.process(2.0)

        val capturedTrip = requireNotNull(trip)
        assertEquals(33.0, capturedTrip.absoluteTemperature)
        assertEquals(30.0, capturedTrip.thermalDeltaCelsius)
        assertEquals(3.0, capturedTrip.ambientCelsius)
        assertEquals(5.0, capturedTrip.deltaPerSecond)
        assertEquals(7.0, capturedTrip.thermalPower)
        assertEquals(4.0, capturedTrip.heatCapacity)
        assertSame(dumpTarget, dumpedTarget)
        assertEquals("Thermal watchdog 33.0°C", dumpReason)
    }

    @Test
    fun `explicit matrix reason and supplier failure are handled like legacy`() {
        val load = ThermalLoad().apply { temperatureCelsius = 10.0 }
        var dumpCount = 0
        val watchdog = ThermalLoadWatchDog(
            load,
            randomFactor = WatchdogRandomFactor { 1.0 },
            diagnosticSink = WatchdogDiagnosticSink { _, _ -> dumpCount++ },
        ).dumpMatrixOnTrip("explicit") { error("unavailable") }
            .setTemperatureLimits(1.0, -1.0)
        watchdog.timeoutReset = 0.0

        watchdog.process(1.0)
        watchdog.process(1.0)

        assertEquals(0, dumpCount)
    }

    @Test
    fun `resistor heat mode selects its independent policy switch`() {
        val categories = mutableListOf<WatchdogType>()
        val target = WatchdogStubDestructible()
        val watchdog = ThermalLoadWatchDog(
            ThermalLoad().apply { temperatureCelsius = 10.0 },
            WatchdogPolicy { categories += it; false },
            WatchdogRandomFactor { 1.0 },
        ).asResistorHeatWatchdog().apply {
            min = -1.0
            max = 1.0
            timeoutReset = 0.0
            setDestroys(target)
        }

        watchdog.process(1.0)
        watchdog.process(1.0)

        assertEquals(listOf(WatchdogType.RESISTOR_HEAT), categories)
        assertEquals(0, target.destructionCount)
    }
}
