package mods.eln.node.six

import mods.eln.sim.ElectricalConnection
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.Simulator
import mods.eln.sim.mna.component.Component
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.misc.MnaConst
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel

/** Level-owned projection of loaded SixNode faces into the pure legacy MNA solver. */
class SixNodeElectricalGraph(private val simulator: Simulator) {
    data class Endpoint(val pos: BlockPos, val face: Direction)
    data class Terminal(val endpoint: Endpoint, val edge: Direction)

    private sealed interface FaceRuntime {
        val loads: List<ElectricalLoad>
        val components: List<Component>
        fun loadAt(edge: Direction): ElectricalLoad?
    }

    private class CableRuntime(val load: ElectricalLoad) : FaceRuntime {
        override val loads = listOf(load)
        override val components = emptyList<Component>()
        override fun loadAt(edge: Direction) = load
    }

    private class SourceRuntime(val load: ElectricalLoad, val source: VoltageSource) : FaceRuntime {
        override val loads = listOf(load)
        override val components = listOf(source)
        override fun loadAt(edge: Direction) = load
    }

    private class ResistorRuntime(
        val aLoad: ElectricalLoad,
        val bLoad: ElectricalLoad,
        val resistor: Resistor,
        val aEdge: Direction,
        val bEdge: Direction,
    ) : FaceRuntime {
        override val loads = listOf(aLoad, bLoad)
        override val components = listOf(resistor)
        override fun loadAt(edge: Direction): ElectricalLoad? = when (edge) {
            aEdge -> aLoad
            bEdge -> bLoad
            else -> null
        }
    }

    private data class RuntimeEntry(val component: MountedSixNodeComponent, val runtime: FaceRuntime)
    private data class ConnectionKey(val first: Terminal, val second: Terminal)

    private val mountedByHost = linkedMapOf<BlockPos, Map<Direction, MountedSixNodeComponent>>()
    private val runtimeByEndpoint = linkedMapOf<Endpoint, RuntimeEntry>()
    private val connections = linkedMapOf<ConnectionKey, ElectricalConnection>()

    val cableCount: Int get() = runtimeByEndpoint.values.count { it.runtime is CableRuntime }
    val componentCount: Int get() = runtimeByEndpoint.size
    val connectionCount: Int get() = connections.size

    fun attachHost(pos: BlockPos, contents: Map<Direction, MountedSixNodeComponent>) {
        mountedByHost[pos.immutable()] = contents.toMap()
        reconcile()
    }

    fun updateHost(pos: BlockPos, contents: Map<Direction, MountedSixNodeComponent>) {
        val key = pos.immutable()
        if (contents.isEmpty()) mountedByHost.remove(key) else mountedByHost[key] = contents.toMap()
        reconcile()
    }

    fun detachHost(pos: BlockPos) {
        if (mountedByHost.remove(pos) != null) reconcile()
    }

    fun detachChunk(chunkX: Int, chunkZ: Int) {
        val removed = mountedByHost.keys.removeIf { (it.x shr 4) == chunkX && (it.z shr 4) == chunkZ }
        if (removed) reconcile()
    }

    fun pruneUnloadedChunks(level: ServerLevel) {
        val removed = mountedByHost.keys.removeIf {
            level.chunkSource.getChunkNow(it.x shr 4, it.z shr 4) == null
        }
        if (removed) reconcile()
    }

    fun clear() {
        removeConnections()
        runtimeByEndpoint.values.forEach(::removeRuntime)
        runtimeByEndpoint.clear()
        mountedByHost.clear()
    }

    /** Compatibility accessor; for a resistor this is the legacy A/right load. */
    fun loadAt(pos: BlockPos, face: Direction): ElectricalLoad? =
        runtimeByEndpoint[Endpoint(pos, face)]?.runtime?.loads?.firstOrNull()

    fun voltageAt(terminal: Terminal): Double? = terminalLoad(terminal)?.state

    fun sourceCurrentAt(pos: BlockPos, face: Direction): Double? =
        (runtimeByEndpoint[Endpoint(pos, face)]?.runtime as? SourceRuntime)?.source?.current

    fun resistorCurrentAt(pos: BlockPos, face: Direction): Double? =
        (runtimeByEndpoint[Endpoint(pos, face)]?.runtime as? ResistorRuntime)?.resistor?.current

    fun hasConnection(first: Endpoint, second: Endpoint): Boolean = connections.keys.any {
        setOf(it.first.endpoint, it.second.endpoint) == setOf(first, second)
    }

    fun hasTerminalConnection(first: Terminal, second: Terminal): Boolean =
        connections.containsKey(connectionKey(first, second))

    private fun reconcile() {
        // RootSystem preserves legacy pin references, so links and internal bipoles are removed first.
        removeConnections()

        val desired = mountedByHost.flatMap { (pos, contents) ->
            contents.map { (face, component) -> Endpoint(pos, face) to component }
        }.filter { (_, component) -> isElectrical(component) }.toMap()

        runtimeByEndpoint.entries.removeIf { (endpoint, entry) ->
            val replacement = desired[endpoint]
            val remove = replacement == null || replacement != entry.component
            if (remove) removeRuntime(entry)
            remove
        }

        (desired.keys - runtimeByEndpoint.keys).sortedWith(endpointOrder).forEach { endpoint ->
            val component = desired.getValue(endpoint)
            val runtime = createRuntime(endpoint.face, component)
            runtime.loads.forEach(simulator::addElectricalLoad)
            runtime.components.forEach(simulator::addElectricalComponent)
            runtimeByEndpoint[endpoint] = RuntimeEntry(component, runtime)
        }

        runtimeByEndpoint.keys.sortedWith(endpointOrder).forEach { endpoint ->
            tangentDirections(endpoint.face).forEach { edge ->
                connectIfPresent(
                    Terminal(endpoint, edge),
                    Terminal(Endpoint(endpoint.pos.relative(edge), endpoint.face), edge.opposite),
                )
                connectIfPresent(
                    Terminal(endpoint, edge),
                    Terminal(Endpoint(endpoint.pos, edge), endpoint.face),
                )
                connectIfPresent(
                    Terminal(endpoint, edge),
                    Terminal(
                        Endpoint(endpoint.pos.relative(endpoint.face).relative(edge), edge.opposite),
                        endpoint.face.opposite,
                    ),
                )
            }
        }
    }

    private fun createRuntime(face: Direction, component: MountedSixNodeComponent): FaceRuntime = when (component.typeId) {
        SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id -> CableRuntime(electricalLoadWithCableResistance())
        SixNodeComponentCatalog.ELECTRICAL_SOURCE.id -> {
            val load = electricalLoadWithCableResistance()
            SourceRuntime(
                load,
                VoltageSource("voltSrc", load, null).setVoltage(
                    component.parameters[VOLTAGE_PARAMETER]?.takeIf(Double::isFinite) ?: 0.0,
                ),
            )
        }
        SixNodeComponentCatalog.POWER_RESISTOR.id -> {
            val aLoad = ElectricalLoad().apply { setSerialResistance(MnaConst.NO_IMPEDANCE) }
            val bLoad = ElectricalLoad().apply { setSerialResistance(MnaConst.NO_IMPEDANCE) }
            ResistorRuntime(
                aLoad,
                bLoad,
                Resistor(aLoad, bLoad).setResistance(
                    component.parameters[RESISTANCE_PARAMETER]
                        ?.takeIf { it.isFinite() && it > 0.0 }
                        ?: EMPTY_RESISTOR_RESISTANCE,
                ),
                worldDirection(face, component.rotation.right()),
                worldDirection(face, component.rotation.left()),
            )
        }
        else -> error("Unsupported electrical SixNode component ${component.typeId}")
    }

    private fun connectIfPresent(first: Terminal, second: Terminal) {
        val firstLoad = terminalLoad(first) ?: return
        val secondLoad = terminalLoad(second) ?: return
        if (firstLoad === secondLoad) return
        val key = connectionKey(first, second)
        if (key in connections) return
        val connection = ElectricalConnection(firstLoad, secondLoad)
        simulator.addElectricalComponent(connection)
        connections[key] = connection
    }

    private fun terminalLoad(terminal: Terminal): ElectricalLoad? {
        if (terminal.edge.axis == terminal.endpoint.face.axis) return null
        return runtimeByEndpoint[terminal.endpoint]?.runtime?.loadAt(terminal.edge)
    }

    private fun removeConnections() {
        connections.values.forEach(simulator::removeElectricalComponent)
        connections.clear()
    }

    private fun removeRuntime(entry: RuntimeEntry) {
        entry.runtime.components.forEach(simulator::removeElectricalComponent)
        entry.runtime.loads.forEach(simulator::removeElectricalLoad)
    }

    private fun electricalLoadWithCableResistance() = ElectricalLoad().apply {
        setSerialResistance(LOW_VOLTAGE_CABLE_RESISTANCE_PER_LOAD)
    }

    private fun isElectrical(component: MountedSixNodeComponent): Boolean = component.typeId in setOf(
        SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id,
        SixNodeComponentCatalog.ELECTRICAL_SOURCE.id,
        SixNodeComponentCatalog.POWER_RESISTOR.id,
    )

    companion object {
        const val LOW_VOLTAGE_CABLE_RESISTANCE_PER_LOAD: Double = 0.0125
        const val EMPTY_RESISTOR_RESISTANCE: Double = 0.01
        const val VOLTAGE_PARAMETER: String = "voltage"
        const val RESISTANCE_PARAMETER: String = "resistance"

        private val endpointOrder = compareBy<Endpoint>({ it.pos.asLong() }, { it.face.ordinal })
        private val terminalOrder = compareBy<Terminal>(
            { it.endpoint.pos.asLong() }, { it.endpoint.face.ordinal }, { it.edge.ordinal },
        )

        @JvmStatic
        fun areLegacyNeighbours(first: Endpoint, second: Endpoint): Boolean {
            if (first == second) return false
            return neighbourTargets(first).contains(second) || neighbourTargets(second).contains(first)
        }

        /** Canonical terminal exposure shared by the server graph and the client renderer. */
        @JvmStatic
        fun supportsTerminal(face: Direction, component: MountedSixNodeComponent, edge: Direction): Boolean {
            if (edge.axis == face.axis) return false
            return when (component.typeId) {
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.id,
                SixNodeComponentCatalog.ELECTRICAL_SOURCE.id -> true
                SixNodeComponentCatalog.POWER_RESISTOR.id -> edge == worldDirection(face, component.rotation.right()) ||
                    edge == worldDirection(face, component.rotation.left())
                else -> false
            }
        }

        private fun neighbourTargets(endpoint: Endpoint): Set<Endpoint> = buildSet {
            tangentDirections(endpoint.face).forEach { edge ->
                add(Endpoint(endpoint.pos.relative(edge), endpoint.face))
                add(Endpoint(endpoint.pos, edge))
                add(Endpoint(endpoint.pos.relative(endpoint.face).relative(edge), edge.opposite))
            }
        }

        private fun connectionKey(first: Terminal, second: Terminal): ConnectionKey =
            if (terminalOrder.compare(first, second) <= 0) ConnectionKey(first, second) else ConnectionKey(second, first)

        private fun tangentDirections(face: Direction): List<Direction> =
            Direction.entries.filter { it.axis != face.axis }

        @JvmStatic
        fun worldDirection(face: Direction, rotation: SixNodeRotation): Direction {
            val right = when (face) {
                Direction.WEST -> Direction.SOUTH
                Direction.EAST -> Direction.NORTH
                Direction.DOWN -> Direction.NORTH
                Direction.UP -> Direction.SOUTH
                Direction.NORTH -> Direction.WEST
                Direction.SOUTH -> Direction.EAST
            }
            val up = when (face) {
                Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH -> Direction.UP
                Direction.DOWN, Direction.UP -> Direction.EAST
            }
            return when (rotation) {
                SixNodeRotation.LEFT -> right.opposite
                SixNodeRotation.RIGHT -> right
                SixNodeRotation.DOWN -> up.opposite
                SixNodeRotation.UP -> up
            }
        }

    }
}
