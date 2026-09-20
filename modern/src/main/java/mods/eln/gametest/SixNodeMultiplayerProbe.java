package mods.eln.gametest;

import java.util.List;
import java.util.Map;
import mods.eln.ElectricalAge;
import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeComponentDefinition;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.node.six.SixNodeRotation;
import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import mods.eln.menu.ElectricalSourceMenu;

/** Development-only dedicated/client probe for SixNode chunk and block-entity synchronization. */
public final class SixNodeMultiplayerProbe {
    private ServerPlayer pendingMenuPlayer;
    private int pendingMenuTicks;
    private ServerPlayer probePlayer;
    private boolean cameraTeleported;
    private int cameraTeleportDelay = -1;
    public static final String MODE_PROPERTY = "eln.sixNodeMultiplayerProbe";
    public static final String SERVER_MODE = "server";
    public static final String CLIENT_MODE = "client";
    public static final BlockPos ORIGIN = new BlockPos(0, -64, 0);
    public static final BlockPos CAMERA_POS = new BlockPos(13, -59, 22);
    public static final String SCREENSHOT_NAME = "six-node-multiplayer.png";
    public static final String MENU_SCREENSHOT_NAME = "electrical-source-menu.png";
    public static final int CONFIG_SOURCE_FACE_INDEX = 0;
    public static final int CONFIG_SOURCE_COMPONENT_INDEX = 1;
    public static final float CONFIGURED_SOURCE_VOLTAGE = 123.5F;

    public static final List<Direction> FACES = List.of(
            Direction.WEST,
            Direction.EAST,
            Direction.DOWN,
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH);
    public static final List<SixNodeComponentDefinition> COMPONENTS = List.of(
            SixNodeComponentCatalog.LOW_VOLTAGE_CABLE,
            SixNodeComponentCatalog.ELECTRICAL_SOURCE,
            SixNodeComponentCatalog.POWER_RESISTOR);
    public static final List<SixNodeRotation> ROTATIONS =
            List.of(SixNodeRotation.UP, SixNodeRotation.RIGHT, SixNodeRotation.DOWN);

    public void onServerStarted(ServerStartedEvent event) {
        if (!SERVER_MODE.equals(System.getProperty(MODE_PROPERTY))) return;

        ServerLevel level = event.getServer().overworld();
        level.setDayTime(6000L);
        level.setWeatherParameters(0, 0, false, false);
        for (int faceIndex = 0; faceIndex < FACES.size(); faceIndex++) {
            Direction face = FACES.get(faceIndex);
            for (int componentIndex = 0; componentIndex < COMPONENTS.size(); componentIndex++) {
                BlockPos hostPos = hostPos(faceIndex, componentIndex);
                level.setBlock(hostPos.relative(face), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
                level.removeBlock(hostPos, false);
                if (!level.setBlock(hostPos, ElnContent.SIX_NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
                    throw new IllegalStateException("Could not create multiplayer SixNode host at " + hostPos);
                }
                if (!(level.getBlockEntity(hostPos) instanceof SixNodeBlockEntity host)) {
                    throw new IllegalStateException("SixNode block entity missing at " + hostPos);
                }
                MountedSixNodeComponent component = component(componentIndex);
                if (!host.mount(face, component)) {
                    throw new IllegalStateException("Could not mount " + component.getTypeId() + " on " + face);
                }
            }
        }
        level.setDefaultSpawnPos(CAMERA_POS, 180.0F);
        event.getServer().saveEverything(true, true, true);
        ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_SERVER_SCENE_READY with 18 synchronized faces");
    }

    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!SERVER_MODE.equals(System.getProperty(MODE_PROPERTY))) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ElnContent.MULTIMETER.get()));
        BlockPos sourcePos = hostPos(CONFIG_SOURCE_FACE_INDEX, CONFIG_SOURCE_COMPONENT_INDEX);
        player.teleportTo(
                level,
                sourcePos.getX() + 2.5,
                sourcePos.getY() + 2.5,
                sourcePos.getZ() + 4.5,
                180.0F,
                10.0F);
        pendingMenuPlayer = player;
        pendingMenuTicks = 20;
        probePlayer = player;
        cameraTeleported = false;
        cameraTeleportDelay = -1;
        ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_SERVER_PLAYER_JOINED: {}", player.getGameProfile().getName());
    }

    public void onServerTick(ServerTickEvent.Post event) {
        if (!SERVER_MODE.equals(System.getProperty(MODE_PROPERTY))) return;
        ServerPlayer player = pendingMenuPlayer;
        if (player != null && pendingMenuTicks-- <= 0) {
            pendingMenuPlayer = null;
            openConfigurationMenu(player);
        }
        if (!cameraTeleported
                && probePlayer != null
                && cameraTeleportDelay < 0
                && configuredVoltageWasApplied(probePlayer.serverLevel())) {
            cameraTeleportDelay = 20;
        }
        if (!cameraTeleported && probePlayer != null && cameraTeleportDelay >= 0 && cameraTeleportDelay-- == 0) {
            cameraTeleported = true;
            probePlayer.teleportTo(
                    probePlayer.serverLevel(),
                    CAMERA_POS.getX() + 0.5,
                    CAMERA_POS.getY() + 3.5,
                    CAMERA_POS.getZ() + 0.5,
                    180.0F,
                    10.0F);
            ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_SERVER_CAMERA_READY");
        }
    }

    private static boolean configuredVoltageWasApplied(ServerLevel level) {
        BlockPos pos = hostPos(CONFIG_SOURCE_FACE_INDEX, CONFIG_SOURCE_COMPONENT_INDEX);
        Direction face = FACES.get(CONFIG_SOURCE_FACE_INDEX);
        if (!(level.getBlockEntity(pos) instanceof SixNodeBlockEntity host)) return false;
        MountedSixNodeComponent source = host.contentsSnapshot().get(face);
        if (source == null) return false;
        return source.getParameters().getOrDefault(SixNodeElectricalGraph.VOLTAGE_PARAMETER, Double.NaN)
                == (double) CONFIGURED_SOURCE_VOLTAGE;
    }

    private static void openConfigurationMenu(ServerPlayer player) {
        BlockPos sourcePos = hostPos(CONFIG_SOURCE_FACE_INDEX, CONFIG_SOURCE_COMPONENT_INDEX);
        Direction sourceFace = FACES.get(CONFIG_SOURCE_FACE_INDEX);
        if (!(player.serverLevel().getBlockEntity(sourcePos) instanceof SixNodeBlockEntity host)) {
            throw new IllegalStateException("Configurable source host missing at " + sourcePos);
        }
        MountedSixNodeComponent source = host.contentsSnapshot().get(sourceFace);
        if (source == null || !ElectricalSourceMenu.open(player, sourcePos, sourceFace, source)) {
            throw new IllegalStateException("Could not open the M3 electrical source menu");
        }
        ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_SERVER_MENU_OPENED");
    }

    public static BlockPos hostPos(int faceIndex, int componentIndex) {
        return ORIGIN.offset(3 + faceIndex * 4, 4, 3 + componentIndex * 4);
    }

    public static MountedSixNodeComponent component(int componentIndex) {
        SixNodeComponentDefinition definition = COMPONENTS.get(componentIndex);
        Map<String, Double> parameters = definition == SixNodeComponentCatalog.ELECTRICAL_SOURCE
                ? Map.of(SixNodeElectricalGraph.VOLTAGE_PARAMETER, 50.0)
                : Map.of();
        return new MountedSixNodeComponent(definition.getId(), ROTATIONS.get(componentIndex), parameters);
    }
}
