package mods.eln.platform;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mods.eln.ElectricalAge;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.sim.Simulator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Owns one simulation context for each concrete Minecraft server instance. */
public final class ServerSimulationLifecycle {
    private static final double CALL_PERIOD = 1.0 / 20.0;
    private static final double ELECTRICAL_PERIOD = 1.0 / 20.0;
    private static final int ELECTRICAL_INTER_SYSTEM_OVERSAMPLING = 50;
    private static final double THERMAL_PERIOD = 1.0 / 400.0;

    private final Map<MinecraftServer, ServerContext> contexts = new IdentityHashMap<>();

    public void onServerStarting(ServerStartingEvent event) {
        var simulator = new Simulator(
                CALL_PERIOD,
                ELECTRICAL_PERIOD,
                ELECTRICAL_INTER_SYSTEM_OVERSAMPLING,
                THERMAL_PERIOD,
                null);
        contexts.put(event.getServer(), new ServerContext(simulator));
        ElectricalAge.LOGGER.info("Created Electrical Age simulation owner for server {}", event.getServer());
    }

    public void onServerTick(ServerTickEvent.Pre event) {
        var context = contexts.get(event.getServer());
        if (context != null) {
            context.simulator.tick();
        }
    }

    public void onServerTickPost(ServerTickEvent.Post event) {
        var context = contexts.get(event.getServer());
        if (context == null) return;
        context.pendingChunkLoads.forEach((level, positions) -> {
            for (ChunkPos position : Set.copyOf(positions)) {
                var chunk = level.getChunkSource().getChunkNow(position.x, position.z);
                if (chunk != null) attachChunk(chunk);
            }
            positions.clear();
        });
        context.graphs.forEach((level, graph) -> graph.pruneUnloadedChunks(level));
    }

    public void onServerStopping(ServerStoppingEvent event) {
        var context = contexts.remove(event.getServer());
        if (context != null) {
            context.graphs.values().forEach(SixNodeElectricalGraph::clear);
            ElectricalAge.LOGGER.info("Removed Electrical Age simulation owner for server {}", event.getServer());
        }
    }

    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        var context = contexts.get(level.getServer());
        if (context != null) {
            attachChunk(chunk);
            context.pendingChunkLoads.computeIfAbsent(level, ignored -> new LinkedHashSet<>()).add(chunk.getPos());
        }
    }

    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var context = contexts.get(level.getServer());
        if (context != null) {
            var hosts = context.loadedHosts.get(level);
            if (hosts != null) {
                hosts.keySet().removeIf(pos ->
                        (pos.getX() >> 4) == event.getChunk().getPos().x
                                && (pos.getZ() >> 4) == event.getChunk().getPos().z);
            }
        }
        var graph = getGraph(level);
        if (graph != null) graph.detachChunk(event.getChunk().getPos().x, event.getChunk().getPos().z);
    }

    public Simulator get(MinecraftServer server) {
        var context = contexts.get(server);
        return context == null ? null : context.simulator;
    }

    public SixNodeElectricalGraph getGraph(ServerLevel level) {
        var context = contexts.get(level.getServer());
        return context == null ? null : context.graphs.get(level);
    }

    public void attach(SixNodeBlockEntity host) {
        rememberHost(host);
        graphFor(host).attachHost(host.getBlockPos(), host.contentsSnapshot());
    }

    public void update(SixNodeBlockEntity host) {
        rememberHost(host);
        graphFor(host).updateHost(host.getBlockPos(), host.contentsSnapshot());
    }

    public void detach(SixNodeBlockEntity host) {
        if (!(host.getLevel() instanceof ServerLevel level)) return;
        var context = contexts.get(level.getServer());
        if (context == null) return;
        var hosts = context.loadedHosts.get(level);
        SixNodeBlockEntity current = hosts == null ? null : hosts.get(host.getBlockPos());
        // A stale callback must not tear down runtime reconstructed for a replacement instance.
        if (current != null && current != host) return;
        if (current == host) hosts.remove(host.getBlockPos());
        var graph = getGraph(level);
        if (graph != null) graph.detachHost(host.getBlockPos());
    }

    public int size() {
        return contexts.size();
    }

    private SixNodeElectricalGraph graphFor(SixNodeBlockEntity host) {
        if (!(host.getLevel() instanceof ServerLevel level)) {
            throw new IllegalStateException("Cannot attach a SixNode without a server level");
        }
        var context = contexts.get(level.getServer());
        if (context == null) {
            throw new IllegalStateException("Cannot attach a SixNode before its server simulation exists");
        }
        return context.graphs.computeIfAbsent(level, ignored -> new SixNodeElectricalGraph(context.simulator));
    }

    private void rememberHost(SixNodeBlockEntity host) {
        if (!(host.getLevel() instanceof ServerLevel level)) return;
        var context = contexts.get(level.getServer());
        if (context == null) return;
        context.loadedHosts
                .computeIfAbsent(level, ignored -> new java.util.HashMap<>())
                .put(host.getBlockPos().immutable(), host);
    }

    private void attachChunk(LevelChunk chunk) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SixNodeBlockEntity host) attach(host);
        }
    }

    private static final class ServerContext {
        private final Simulator simulator;
        private final Map<ServerLevel, SixNodeElectricalGraph> graphs = new IdentityHashMap<>();
        private final Map<ServerLevel, Set<ChunkPos>> pendingChunkLoads = new IdentityHashMap<>();
        private final Map<ServerLevel, Map<BlockPos, SixNodeBlockEntity>> loadedHosts = new IdentityHashMap<>();

        private ServerContext(Simulator simulator) {
            this.simulator = simulator;
        }
    }
}
