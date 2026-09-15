package mods.eln.gametest;

import java.util.Map;
import mods.eln.ElectricalAge;
import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.node.six.SixNodeRotation;
import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two-process development probe for a real region-file save and reopen. */
public final class SixNodeDiskPersistenceProbe {
    public static final String MODE_PROPERTY = "eln.sixNodePersistenceProbe";
    public static final String WRITE_MODE = "write";
    public static final String VERIFY_MODE = "verify";

    private static final BlockPos HOST_POS = new BlockPos(1608, 300, 1608);
    private static final Direction MOUNTED_FACE = Direction.DOWN;
    private static final MountedSixNodeComponent EXPECTED_COMPONENT = new MountedSixNodeComponent(
            SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.RIGHT);

    private MinecraftServer awaitingChunkUnload;
    private MinecraftServer awaitingGraphReload;
    private int chunkUnloadWaitTicks;
    private int graphReloadWaitTicks;

    public void onServerStarted(ServerStartedEvent event) {
        String mode = System.getProperty(MODE_PROPERTY);
        if (mode == null) return;

        MinecraftServer server = event.getServer();
        try {
            switch (mode) {
                case WRITE_MODE -> writeAndSave(server);
                case VERIFY_MODE -> {
                    verifyReopenedWorld(server);
                    beginChunkUnloadCheck(server);
                    return;
                }
                default -> throw new IllegalStateException("Unknown SixNode persistence probe mode: " + mode);
            }
        } catch (RuntimeException exception) {
            ElectricalAge.LOGGER.error("SixNode disk persistence probe failed in mode {}", mode, exception);
            throw exception;
        }

        server.halt(false);
    }

    public void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == awaitingGraphReload) {
            ServerLevel level = event.getServer().overworld();
            graphReloadWaitTicks++;
            if (!isGraphLoaded(level)) {
                if (graphReloadWaitTicks <= 40) return;
                awaitingGraphReload = null;
                verifyGraph(level, true, "Reloaded chunk after 40 ticks");
            }
            awaitingGraphReload = null;
            verifyGraph(level, true, "Reloaded chunk");
            ChunkPos reloadedChunk = new ChunkPos(HOST_POS);
            level.setChunkForced(reloadedChunk.x, reloadedChunk.z, false);
            ElectricalAge.LOGGER.info("SIX_NODE_CHUNK_UNLOAD_RELOAD_OK at {}", HOST_POS);
            event.getServer().halt(false);
            return;
        }
        if (event.getServer() != awaitingChunkUnload) return;

        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        ChunkPos chunkPos = new ChunkPos(HOST_POS);
        chunkUnloadWaitTicks++;

        if (level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            awaitingChunkUnload = null;
            verifyGraph(level, false, "Unloaded chunk");
            level.setChunkForced(chunkPos.x, chunkPos.z, true);
            level.getChunkAt(HOST_POS);
            verifyContents(level, "Reloaded chunk");
            awaitingGraphReload = server;
            graphReloadWaitTicks = 0;
            return;
        }

        if (chunkUnloadWaitTicks > 600) {
            awaitingChunkUnload = null;
            IllegalStateException exception =
                    new IllegalStateException("Persistence probe chunk did not unload within 600 server ticks");
            ElectricalAge.LOGGER.error("SixNode chunk unload/reload probe failed", exception);
            throw exception;
        }
    }

    private static void writeAndSave(MinecraftServer server) {
        ServerLevel level = server.overworld();
        level.getChunkAt(HOST_POS);
        level.setBlock(HOST_POS.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(HOST_POS) instanceof SixNodeBlockEntity previousHost) {
            for (Direction face : previousHost.contentsSnapshot().keySet()) previousHost.unmount(face);
        }
        level.removeBlock(HOST_POS, false);
        if (!level.setBlock(HOST_POS, ElnContent.SIX_NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
            throw new IllegalStateException("Could not create the SixNode persistence probe host");
        }

        if (!(level.getBlockEntity(HOST_POS) instanceof SixNodeBlockEntity host)) {
            throw new IllegalStateException("Registered SixNode block entity was not created");
        }
        if (!host.mount(MOUNTED_FACE, EXPECTED_COMPONENT)) {
            throw new IllegalStateException("Fresh SixNode host rejected the persistence probe cable");
        }
        verifyGraph(level, true, "Written chunk");
        if (!server.saveEverything(true, true, true)) {
            throw new IllegalStateException("MinecraftServer.saveEverything reported failure");
        }
        ElectricalAge.LOGGER.info("SIX_NODE_PERSISTENCE_WRITE_OK at {}", HOST_POS);
    }

    private static void verifyReopenedWorld(MinecraftServer server) {
        ServerLevel level = server.overworld();
        level.getChunkAt(HOST_POS);

        verifyContents(level, "Reopened chunk");
        verifyGraph(level, true, "Reopened chunk");
        ElectricalAge.LOGGER.info("SIX_NODE_PERSISTENCE_VERIFY_OK at {}", HOST_POS);
    }

    private void beginChunkUnloadCheck(MinecraftServer server) {
        ServerLevel level = server.overworld();
        ChunkPos chunkPos = new ChunkPos(HOST_POS);
        level.setChunkForced(chunkPos.x, chunkPos.z, true);
        level.setChunkForced(chunkPos.x, chunkPos.z, false);
        awaitingChunkUnload = server;
        chunkUnloadWaitTicks = 0;
    }

    private static void verifyContents(ServerLevel level, String phase) {
        if (!level.getBlockState(HOST_POS).is(ElnContent.SIX_NODE.get())) {
            throw new IllegalStateException(phase + " does not contain eln:six_node at " + HOST_POS);
        }
        if (!(level.getBlockEntity(HOST_POS) instanceof SixNodeBlockEntity host)) {
            throw new IllegalStateException(phase + " does not contain the registered SixNode block entity");
        }

        Map<Direction, MountedSixNodeComponent> contents = host.contentsSnapshot();
        if (contents.size() != 1 || !EXPECTED_COMPONENT.equals(contents.get(MOUNTED_FACE))) {
            throw new IllegalStateException(phase + " SixNode contents differ from the saved face: " + contents);
        }
    }

    private static void verifyGraph(ServerLevel level, boolean expectedLoaded, String phase) {
        boolean loaded = isGraphLoaded(level);
        if (loaded != expectedLoaded) {
            throw new IllegalStateException(
                    phase + " electrical graph load state was " + loaded + ", expected " + expectedLoaded);
        }
    }

    private static boolean isGraphLoaded(ServerLevel level) {
        SixNodeElectricalGraph graph = ElectricalAge.simulationLifecycle().getGraph(level);
        return graph != null && graph.loadAt(HOST_POS, MOUNTED_FACE) != null;
    }
}
