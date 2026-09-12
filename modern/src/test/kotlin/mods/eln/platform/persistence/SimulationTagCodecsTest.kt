package mods.eln.platform.persistence

import mods.eln.misc.FunctionTable
import mods.eln.sim.BatteryProcess
import mods.eln.sim.FurnaceProcess
import mods.eln.sim.RegulatorProcess
import mods.eln.sim.RegulatorState
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.CurrentSource
import mods.eln.sim.mna.component.Inductor
import mods.eln.sim.mna.component.PowerSource
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.process.PowerSourceBipole
import mods.eln.sim.mna.state.VoltageState
import net.minecraft.nbt.CompoundTag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimulationTagCodecsTest {
    @Test
    fun `battery codec preserves legacy keys and repairs invalid state`() {
        val source = battery("source").apply { Q = 2.5; life = 0.75 }
        val tag = CompoundTag()
        BatteryProcessTagCodec.write(tag, "pfx", source)
        assertEquals(2.5, tag.getDouble("pfxNBPQ"))
        assertEquals(0.75, tag.getDouble("pfxNBPlife"))

        tag.putDouble("pfxNBPQ", Double.NaN)
        tag.putDouble("pfxNBPlife", Double.POSITIVE_INFINITY)
        val restored = battery("restored")
        BatteryProcessTagCodec.read(tag, "pfx", restored)
        assertEquals(0.0, restored.Q)
        assertEquals(1.0, restored.life)
    }

    @Test
    fun `regulator codec preserves named keys and legacy nan repair`() {
        val source = TestRegulator("heater").apply { target = 42.5; restoreForTest(0.125) }
        val tag = CompoundTag()
        RegulatorProcessTagCodec.write(tag, "node.", source)
        assertEquals(0.125, tag.getDouble("node.heatererrorIntegrated"))
        assertEquals(42.5, tag.getDouble("node.heatertarget"))

        tag.putDouble("node.heatererrorIntegrated", Double.NaN)
        val restored = TestRegulator("heater")
        RegulatorProcessTagCodec.read(tag, "node.", restored)
        assertEquals(0.0, restored.errorIntegrated)
        assertEquals(42.5, restored.target)
    }

    @Test
    fun `voltage and thermal codecs retain legacy float precision`() {
        val voltage = VoltageState().apply { this.voltage = 12.345678901 }
        val thermal = ThermalLoad(87.65432109)
        val tag = CompoundTag()
        VoltageStateTagCodec.write(tag, "p.", "load", voltage)
        ThermalLoadTagCodec.write(tag, "p.", "case", thermal)
        assertEquals(12.345678901f, tag.getFloat("p.loadUc"))
        assertEquals(87.65432109f, tag.getFloat("p.caseTc"))

        val restoredVoltage = VoltageState()
        val restoredThermal = ThermalLoad()
        VoltageStateTagCodec.read(tag, "p.", "load", restoredVoltage)
        ThermalLoadTagCodec.read(tag, "p.", "case", restoredThermal)
        assertEquals(12.345678901f.toDouble(), restoredVoltage.voltage)
        assertEquals(87.65432109f.toDouble(), restoredThermal.temperatureCelsius)

        tag.putFloat("p.loadUc", Float.NaN)
        tag.putFloat("p.caseTc", Float.POSITIVE_INFINITY)
        VoltageStateTagCodec.read(tag, "p.", "load", restoredVoltage)
        ThermalLoadTagCodec.read(tag, "p.", "case", restoredThermal)
        assertEquals(0.0, restoredVoltage.voltage)
        assertEquals(0.0, restoredThermal.temperatureCelsius)
    }

    @Test
    fun `furnace codec keeps float fuel energy and bounded gain`() {
        val source = FurnaceProcess(ThermalLoad()).apply {
            combustibleEnergy = 123.456789
            setGain(0.625)
        }
        val tag = CompoundTag()
        FurnaceProcessTagCodec.write(tag, "p.", "furnace", source)
        assertEquals(123.456789f, tag.getFloat("p.furnaceQ"))
        assertEquals(0.625, tag.getDouble("p.furnacegain"))

        tag.putDouble("p.furnacegain", 2.0)
        val restored = FurnaceProcess(ThermalLoad())
        FurnaceProcessTagCodec.read(tag, "p.", "furnace", restored)
        assertEquals(123.456789f.toDouble(), restored.combustibleEnergy)
        assertEquals(1.0, restored.gain)
    }

    @Test
    fun `mna source and inductor codecs preserve exact legacy fields`() {
        val voltage = VoltageSource("vs").setVoltage(24.0).apply { currentState.state = -3.0 }
        val current = CurrentSource("cs").setCurrent(4.0)
        val inductor = Inductor("coil").apply { currentState.state = 5.0 }
        val tag = CompoundTag()
        VoltageSourceTagCodec.write(tag, "n.", voltage)
        CurrentSourceTagCodec.write(tag, "n.", current)
        InductorTagCodec.write(tag, "n.", inductor)

        val restoredVoltage = VoltageSource("vs")
        val restoredCurrent = CurrentSource("cs")
        val restoredInductor = Inductor("coil")
        VoltageSourceTagCodec.read(tag, "n.", restoredVoltage)
        CurrentSourceTagCodec.read(tag, "n.", restoredCurrent)
        InductorTagCodec.read(tag, "n.", restoredInductor)
        assertEquals(24.0, restoredVoltage.voltage)
        assertEquals(-3.0, restoredVoltage.currentState.state)
        assertEquals(4.0, restoredCurrent.current)
        assertEquals(5.0, restoredInductor.currentState.state)
        assertTrue(tag.contains("n.vsU"))
        assertTrue(tag.contains("n.vsIstate"))
        assertTrue(tag.contains("n.csI"))
        assertTrue(tag.contains("n.coilIstate"))
    }

    @Test
    fun `resistor switch codec restores base resistance and switch state`() {
        val source = ResistorSwitch("switch").apply {
            setOffResistance(9_000.0)
            setResistance(7.5)
            setState(true)
        }
        val tag = CompoundTag()
        ResistorSwitchTagCodec.write(tag, "n.", source)

        val restored = ResistorSwitch("switch").apply { setOffResistance(9_000.0) }
        ResistorSwitchTagCodec.read(tag, "n.", restored)
        assertEquals(7.5, restored.baseResistance)
        assertEquals(7.5, restored.resistance)
        assertTrue(restored.state)

        tag.putDouble("n.switchR", Double.NaN)
        tag.putBoolean("n.switchState", false)
        ResistorSwitchTagCodec.read(tag, "n.", restored)
        assertEquals(9_000.0, restored.baseResistance)
        assertEquals(9_000.0, restored.resistance)
        assertFalse(restored.state)
    }

    @Test
    fun `power source codecs preserve their legacy limits`() {
        val source = PowerSource("generator", VoltageState()).apply {
            setVoltage(230.0)
            currentState.state = -2.0
            setPower(450.0)
            setMaximums(250.0, 3.0)
        }
        val a = VoltageState()
        val b = VoltageState()
        val bipole = PowerSourceBipole(a, b, VoltageSource("a", a), VoltageSource("b", b)).apply {
            setPower(900.0)
            setMaximums(400.0, 5.0)
        }
        val tag = CompoundTag()
        PowerSourceTagCodec.write(tag, "n.", source)
        PowerSourceBipoleTagCodec.write(tag, "n.bipole.", bipole)

        val restoredSource = PowerSource("generator", VoltageState())
        val restoredBipole = PowerSourceBipole(
            VoltageState(), VoltageState(), VoltageSource("a"), VoltageSource("b"),
        )
        PowerSourceTagCodec.read(tag, "n.", restoredSource)
        PowerSourceBipoleTagCodec.read(tag, "n.bipole.", restoredBipole)
        assertEquals(230.0, restoredSource.voltage)
        assertEquals(-2.0, restoredSource.currentState.state)
        assertEquals(450.0, restoredSource.power)
        assertEquals(250.0, restoredSource.maximumVoltage)
        assertEquals(3.0, restoredSource.maximumCurrent)
        assertEquals(900.0, restoredBipole.power)
        assertEquals(400.0, restoredBipole.maximumVoltage)
        assertEquals(5.0, restoredBipole.maximumCurrent)
    }

    @Test
    fun `resistor codec intentionally ignores name like 1 24 8`() {
        val source = Resistor().setResistance(12.0)
        val tag = CompoundTag()
        ResistorTagCodec.write(tag, "pfx", "firstName", source)
        assertTrue(tag.contains("pfxR"))
        assertFalse(tag.contains("pfxfirstNameR"))
        val restored = Resistor()
        ResistorTagCodec.read(tag, "pfx", "anotherName", restored)
        assertEquals(12.0, restored.resistance)
    }

    private fun battery(name: String) = BatteryProcess(
        null, null, FunctionTable(doubleArrayOf(1.0, 1.0), 1.0), 10.0, VoltageSource(name), ThermalLoad(),
    )

    private class TestRegulator(name: String) : RegulatorProcess(name) {
        override fun getHit(): Double = 0.0
        override fun setCmd(cmd: Double) = Unit
        fun restoreForTest(errorIntegrated: Double) {
            restoreState(RegulatorState(errorIntegrated, target))
        }
    }
}
