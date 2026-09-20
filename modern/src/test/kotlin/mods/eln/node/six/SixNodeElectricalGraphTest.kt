package mods.eln.node.six

import mods.eln.sim.Simulator
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SixNodeElectricalGraphTest {
    private lateinit var simulator: Simulator
    private lateinit var graph: SixNodeElectricalGraph

    @BeforeEach
    fun setUp() {
        simulator = Simulator(0.05, 0.05, 50, 0.0025)
        graph = SixNodeElectricalGraph(simulator)
    }

    @AfterEach
    fun tearDown() = graph.clear()

    @Test
    fun `loaded cable face owns a legacy-configured electrical load`() {
        val pos = BlockPos(10, 20, 30)
        graph.attachHost(pos, mapOf(Direction.DOWN to cable()))

        val load = graph.loadAt(pos, Direction.DOWN)
        assertNotNull(load)
        requireNotNull(load)
        assertEquals(0.0125, load.serialResistance, 0.0)
        assertTrue(simulator.mna.isRegistered(load))
        assertEquals(1, graph.cableCount)
        assertEquals(0, graph.connectionCount)
    }

    @Test
    fun `coplanar adjacent cable faces create one electrical segment`() {
        val first = BlockPos.ZERO
        val second = first.east()
        graph.attachHost(first, mapOf(Direction.DOWN to cable()))
        graph.attachHost(second, mapOf(Direction.DOWN to cable()))

        assertTrue(graph.hasConnection(endpoint(first, Direction.DOWN), endpoint(second, Direction.DOWN)))
        assertEquals(1, graph.connectionCount)
    }

    @Test
    fun `orthogonal faces connect internally and around an outer corner`() {
        val origin = BlockPos.ZERO
        graph.attachHost(origin, mapOf(Direction.DOWN to cable(), Direction.EAST to cable()))
        assertTrue(graph.hasConnection(endpoint(origin, Direction.DOWN), endpoint(origin, Direction.EAST)))

        val diagonal = origin.relative(Direction.DOWN).relative(Direction.NORTH)
        graph.attachHost(diagonal, mapOf(Direction.SOUTH to cable()))
        assertTrue(graph.hasConnection(endpoint(origin, Direction.DOWN), endpoint(diagonal, Direction.SOUTH)))
        assertTrue(
            SixNodeElectricalGraph.areLegacyNeighbours(
                endpoint(origin, Direction.DOWN),
                endpoint(diagonal, Direction.SOUTH),
            ),
        )
        assertFalse(
            SixNodeElectricalGraph.areLegacyNeighbours(
                endpoint(origin, Direction.DOWN),
                endpoint(diagonal, Direction.NORTH),
            ),
        )
    }

    @Test
    fun `opposite faces and parallel diagonal faces do not connect`() {
        val origin = BlockPos.ZERO
        graph.attachHost(origin, mapOf(Direction.DOWN to cable(), Direction.UP to cable()))
        graph.attachHost(origin.east().south(), mapOf(Direction.DOWN to cable()))

        assertFalse(graph.hasConnection(endpoint(origin, Direction.DOWN), endpoint(origin, Direction.UP)))
        assertFalse(graph.hasConnection(endpoint(origin, Direction.DOWN), endpoint(origin.east().south(), Direction.DOWN)))
    }

    @Test
    fun `host update and detach remove connections before electrical loads`() {
        val first = BlockPos.ZERO
        val second = first.east()
        graph.attachHost(first, mapOf(Direction.DOWN to cable()))
        graph.attachHost(second, mapOf(Direction.DOWN to cable()))
        val removedLoad = graph.loadAt(second, Direction.DOWN)
        assertNotNull(removedLoad)
        requireNotNull(removedLoad)

        graph.updateHost(second, emptyMap())

        assertNull(graph.loadAt(second, Direction.DOWN))
        assertFalse(simulator.mna.isRegistered(removedLoad))
        assertEquals(0, graph.connectionCount)

        graph.detachHost(first)
        assertEquals(0, graph.cableCount)
    }

    @Test
    fun `chunk detach removes only runtime owned by that chunk`() {
        val firstChunkCable = BlockPos(15, 10, 15)
        val secondChunkCable = BlockPos(16, 10, 15)
        graph.attachHost(firstChunkCable, mapOf(Direction.DOWN to cable()))
        graph.attachHost(secondChunkCable, mapOf(Direction.DOWN to cable()))

        graph.detachChunk(0, 0)

        assertNull(graph.loadAt(firstChunkCable, Direction.DOWN))
        assertNotNull(graph.loadAt(secondChunkCable, Direction.DOWN))
        assertEquals(1, graph.cableCount)
    }

    @Test
    fun `power resistor exposes only its legacy left and right terminals`() {
        val pos = BlockPos.ZERO
        graph.attachHost(pos, mapOf(Direction.DOWN to resistor(SixNodeRotation.LEFT)))
        val endpoint = endpoint(pos, Direction.DOWN)

        assertNotNull(graph.voltageAt(SixNodeElectricalGraph.Terminal(endpoint, Direction.WEST)))
        assertNotNull(graph.voltageAt(SixNodeElectricalGraph.Terminal(endpoint, Direction.EAST)))
        assertNull(graph.voltageAt(SixNodeElectricalGraph.Terminal(endpoint, Direction.NORTH)))
        assertNull(graph.voltageAt(SixNodeElectricalGraph.Terminal(endpoint, Direction.SOUTH)))
    }

    @Test
    fun `terminal exposure used by rendering matches the runtime graph`() {
        val cable = cable()
        Direction.entries.forEach { face ->
            Direction.entries.filter { it.axis != face.axis }.forEach { edge ->
                assertTrue(SixNodeElectricalGraph.supportsTerminal(face, cable, edge))
            }
            assertFalse(SixNodeElectricalGraph.supportsTerminal(face, cable, face))
            assertFalse(SixNodeElectricalGraph.supportsTerminal(face, cable, face.opposite))
        }

        val resistor = resistor(SixNodeRotation.LEFT)
        assertTrue(SixNodeElectricalGraph.supportsTerminal(Direction.DOWN, resistor, Direction.WEST))
        assertTrue(SixNodeElectricalGraph.supportsTerminal(Direction.DOWN, resistor, Direction.EAST))
        assertFalse(SixNodeElectricalGraph.supportsTerminal(Direction.DOWN, resistor, Direction.NORTH))
        assertFalse(SixNodeElectricalGraph.supportsTerminal(Direction.DOWN, resistor, Direction.SOUTH))
    }

    @Test
    fun `legacy lrdu frame remains stable on all six mounting faces`() {
        val expectedRight = mapOf(
            Direction.WEST to Direction.SOUTH,
            Direction.EAST to Direction.NORTH,
            Direction.DOWN to Direction.NORTH,
            Direction.UP to Direction.SOUTH,
            Direction.NORTH to Direction.WEST,
            Direction.SOUTH to Direction.EAST,
        )
        expectedRight.forEach { (face, right) ->
            assertEquals(right, SixNodeElectricalGraph.worldDirection(face, SixNodeRotation.RIGHT))
            assertEquals(right.opposite, SixNodeElectricalGraph.worldDirection(face, SixNodeRotation.LEFT))
        }
    }

    @Test
    fun `source resistor source circuit matches the legacy dc result`() {
        val high = BlockPos.ZERO
        val resistor = high.east()
        val ground = resistor.east()
        graph.attachHost(high, mapOf(Direction.DOWN to source(50.0)))
        graph.attachHost(resistor, mapOf(Direction.DOWN to resistor(SixNodeRotation.LEFT)))
        graph.attachHost(ground, mapOf(Direction.DOWN to source(0.0)))

        simulator.tick()

        val expectedCurrent = 50.0 / (
            SixNodeElectricalGraph.EMPTY_RESISTOR_RESISTANCE +
                2.0 * SixNodeElectricalGraph.LOW_VOLTAGE_CABLE_RESISTANCE_PER_LOAD +
                2.0e-9
            )
        assertEquals(expectedCurrent, kotlin.math.abs(requireNotNull(graph.resistorCurrentAt(resistor, Direction.DOWN))), 1.0e-5)
        assertEquals(50.0, requireNotNull(graph.loadAt(high, Direction.DOWN)).state, 1.0e-9)
        assertEquals(0.0, requireNotNull(graph.loadAt(ground, Direction.DOWN)).state, 1.0e-9)
        assertEquals(2, graph.connectionCount)

        val sourceReading = requireNotNull(graph.measurementAt(high, Direction.DOWN))
        assertEquals(SixNodeElectricalMeasurement.Kind.SOURCE, sourceReading.kind)
        assertEquals(50.0, sourceReading.voltage, 1.0e-9)
        assertEquals(expectedCurrent, kotlin.math.abs(sourceReading.current), 1.0e-5)

        val resistorReading = requireNotNull(graph.measurementAt(resistor, Direction.DOWN))
        assertEquals(SixNodeElectricalMeasurement.Kind.RESISTOR, resistorReading.kind)
        assertEquals(-expectedCurrent * SixNodeElectricalGraph.EMPTY_RESISTOR_RESISTANCE, resistorReading.voltage, 1.0e-5)
        assertEquals(expectedCurrent, resistorReading.current, 1.0e-5)
        assertEquals(SixNodeElectricalGraph.EMPTY_RESISTOR_RESISTANCE, resistorReading.resistance, 0.0)
    }

    @Test
    fun `cable measurement includes the legacy serial power loss`() {
        val pos = BlockPos.ZERO
        graph.attachHost(pos, mapOf(Direction.DOWN to cable()))
        simulator.tick()

        val reading = requireNotNull(graph.measurementAt(pos, Direction.DOWN))
        assertEquals(SixNodeElectricalMeasurement.Kind.CABLE, reading.kind)
        assertEquals(0.0, reading.voltage, 0.0)
        assertEquals(0.0, reading.current, 0.0)
        assertEquals(0.0, reading.cablePowerLoss, 0.0)
        assertNull(graph.measurementAt(pos, Direction.UP))
    }

    private fun cable() = MountedSixNodeComponent(
        SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id,
        SixNodeRotation.LEFT,
    )

    private fun source(voltage: Double) = MountedSixNodeComponent(
        SixNodeComponentCatalog.ELECTRICAL_SOURCE.id,
        SixNodeRotation.LEFT,
        mapOf(SixNodeElectricalGraph.VOLTAGE_PARAMETER to voltage),
    )

    private fun resistor(rotation: SixNodeRotation) = MountedSixNodeComponent(
        SixNodeComponentCatalog.POWER_RESISTOR.id,
        rotation,
    )

    private fun endpoint(pos: BlockPos, face: Direction) = SixNodeElectricalGraph.Endpoint(pos, face)
}
