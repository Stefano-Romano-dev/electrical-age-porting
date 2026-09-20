package mods.eln.client;

import java.util.Map;
import mods.eln.ElectricalAge;
import mods.eln.PortingBaseline;
import mods.eln.client.screen.ElectricalSourceScreen;
import mods.eln.gametest.SixNodeMultiplayerProbe;
import mods.eln.node.six.MountedSixNodeComponent;
import mods.eln.node.six.SixNodeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import mods.eln.menu.ElectricalSourceMenu;
import mods.eln.node.six.SixNodeElectricalGraph;
import mods.eln.registry.ElnContent;

/** Client half of the development-only dedicated multiplayer synchronization probe. */
@EventBusSubscriber(modid = PortingBaseline.MOD_ID, value = Dist.CLIENT)
public final class SixNodeMultiplayerClientProbe {
    private static int synchronizedTicks;
    private static int menuTicks;
    private static boolean connectionStarted;
    private static boolean screenshotRequested;
    private static boolean configurationSent;
    private static boolean configurationSynchronized;
    private static int inWorldTicks;
    private static boolean menuStateLogged;
    private static int configurationMenuTicks;
    private static boolean menuScreenshotRequested;
    private static boolean multimeterUsed;

    private SixNodeMultiplayerClientProbe() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!SixNodeMultiplayerProbe.CLIENT_MODE.equals(
                System.getProperty(SixNodeMultiplayerProbe.MODE_PROPERTY))) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            if (!connectionStarted && minecraft.getOverlay() == null && minecraft.screen != null && ++menuTicks >= 20) {
                connectionStarted = true;
                String address = "localhost:25565";
                ConnectScreen.startConnecting(
                        minecraft.screen,
                        minecraft,
                        ServerAddress.parseString(address),
                        new ServerData("Electrical Age M2 probe", address, ServerData.Type.OTHER),
                        true,
                        null);
            }
            return;
        }

        inWorldTicks++;
        if (!minecraft.player.getMainHandItem().is(ElnContent.MULTIMETER.get())) {
            if (inWorldTicks > 40) throw new IllegalStateException("Client did not receive the synchronized multimeter");
            return;
        }
        if (!configurationSent) {
            verifySynchronizedScene(minecraft, false);
            if (!menuStateLogged && inWorldTicks >= 40) {
                menuStateLogged = true;
                ElectricalAge.LOGGER.info(
                        "SIX_NODE_MULTIPLAYER_CLIENT_MENU_STATE: menu={}, screen={}",
                        minecraft.player.containerMenu.getClass().getName(),
                        minecraft.screen == null ? "<none>" : minecraft.screen.getClass().getName());
            }
            if (minecraft.player.containerMenu instanceof ElectricalSourceMenu
                    && minecraft.screen instanceof ElectricalSourceScreen screen) {
                configurationMenuTicks++;
                if (!menuScreenshotRequested && configurationMenuTicks >= 5) {
                    menuScreenshotRequested = true;
                    Screenshot.grab(
                            minecraft.gameDirectory,
                            SixNodeMultiplayerProbe.MENU_SCREENSHOT_NAME,
                            minecraft.getMainRenderTarget(),
                            message -> ElectricalAge.LOGGER.info(
                                    "SIX_NODE_MULTIPLAYER_CLIENT_MENU_SCREENSHOT: {}", message.getString()));
                }
                if (configurationMenuTicks >= 10) {
                    configurationSent = true;
                    screen.commitVoltageForDevelopmentProbe(SixNodeMultiplayerProbe.CONFIGURED_SOURCE_VOLTAGE);
                    ElectricalAge.LOGGER.info(
                            "SIX_NODE_MULTIPLAYER_CLIENT_CONFIGURATION_COMMITTED_BY_WIDGET");
                }
            }
            if (!configurationSent && inWorldTicks > 200) {
                throw new IllegalStateException("Timed out waiting for the electrical source menu");
            }
            return;
        }
        if (!configurationSynchronized) {
            configurationSynchronized = configuredVoltageIsVisible(minecraft);
            if (!configurationSynchronized) {
                verifySynchronizedScene(minecraft, false);
                if (inWorldTicks > 200) throw new IllegalStateException("Timed out waiting for configured source voltage");
                return;
            }
            minecraft.player.closeContainer();
            ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_CLIENT_CONFIGURATION_SYNC_OK");
        }
        if (!multimeterUsed) {
            multimeterUsed = true;
            var sourcePos = SixNodeMultiplayerProbe.hostPos(
                    SixNodeMultiplayerProbe.CONFIG_SOURCE_FACE_INDEX,
                    SixNodeMultiplayerProbe.CONFIG_SOURCE_COMPONENT_INDEX);
            Direction sourceFace = SixNodeMultiplayerProbe.FACES.get(SixNodeMultiplayerProbe.CONFIG_SOURCE_FACE_INDEX);
            minecraft.gameMode.useItemOn(
                    minecraft.player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(sourcePos), sourceFace, sourcePos, false));
            ElectricalAge.LOGGER.info("SIX_NODE_MULTIPLAYER_CLIENT_MULTIMETER_USED");
        }
        verifySynchronizedScene(minecraft, true);
        synchronizedTicks++;
        minecraft.player.setYRot(180.0F);
        minecraft.player.setXRot(10.0F);
        minecraft.options.hideGui = true;
        if (!screenshotRequested && synchronizedTicks >= 20) {
            screenshotRequested = true;
            Screenshot.grab(
                    minecraft.gameDirectory,
                    SixNodeMultiplayerProbe.SCREENSHOT_NAME,
                    minecraft.getMainRenderTarget(),
                    message -> ElectricalAge.LOGGER.info(
                            "SIX_NODE_MULTIPLAYER_CLIENT_SCREENSHOT: {}", message.getString()));
        }
        if (synchronizedTicks >= 60) {
            ElectricalAge.LOGGER.info(
                    "SIX_NODE_MULTIPLAYER_CLIENT_SYNC_OK with 18 synchronized faces and configured voltage {}",
                    SixNodeMultiplayerProbe.CONFIGURED_SOURCE_VOLTAGE);
            minecraft.stop();
        }
    }

    private static void verifySynchronizedScene(Minecraft minecraft, boolean expectConfiguredVoltage) {
        for (int faceIndex = 0; faceIndex < SixNodeMultiplayerProbe.FACES.size(); faceIndex++) {
            Direction face = SixNodeMultiplayerProbe.FACES.get(faceIndex);
            for (int componentIndex = 0;
                    componentIndex < SixNodeMultiplayerProbe.COMPONENTS.size();
                    componentIndex++) {
                if (!(minecraft.level.getBlockEntity(SixNodeMultiplayerProbe.hostPos(faceIndex, componentIndex))
                        instanceof SixNodeBlockEntity host)) {
                    throw new IllegalStateException("Client is missing synchronized SixNode at "
                            + SixNodeMultiplayerProbe.hostPos(faceIndex, componentIndex));
                }
                MountedSixNodeComponent expected = SixNodeMultiplayerProbe.component(componentIndex);
                if (expectConfiguredVoltage
                        && faceIndex == SixNodeMultiplayerProbe.CONFIG_SOURCE_FACE_INDEX
                        && componentIndex == SixNodeMultiplayerProbe.CONFIG_SOURCE_COMPONENT_INDEX) {
                    expected = new MountedSixNodeComponent(
                            expected.getTypeId(),
                            expected.getRotation(),
                            Map.of(
                                    SixNodeElectricalGraph.VOLTAGE_PARAMETER,
                                    (double) SixNodeMultiplayerProbe.CONFIGURED_SOURCE_VOLTAGE));
                }
                if (!host.contentsSnapshot().equals(Map.of(face, expected))) {
                    throw new IllegalStateException("Client SixNode state differs at "
                            + SixNodeMultiplayerProbe.hostPos(faceIndex, componentIndex)
                            + ": "
                            + host.contentsSnapshot());
                }
            }
        }
    }

    private static boolean configuredVoltageIsVisible(Minecraft minecraft) {
        Direction face = SixNodeMultiplayerProbe.FACES.get(SixNodeMultiplayerProbe.CONFIG_SOURCE_FACE_INDEX);
        if (!(minecraft.level.getBlockEntity(SixNodeMultiplayerProbe.hostPos(
                        SixNodeMultiplayerProbe.CONFIG_SOURCE_FACE_INDEX,
                        SixNodeMultiplayerProbe.CONFIG_SOURCE_COMPONENT_INDEX))
                instanceof SixNodeBlockEntity host)) return false;
        MountedSixNodeComponent component = host.contentsSnapshot().get(face);
        if (component == null) return false;
        return component.getParameters().getOrDefault(SixNodeElectricalGraph.VOLTAGE_PARAMETER, Double.NaN)
                == (double) SixNodeMultiplayerProbe.CONFIGURED_SOURCE_VOLTAGE;
    }
}
