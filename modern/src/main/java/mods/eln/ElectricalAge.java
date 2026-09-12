package mods.eln;

import com.mojang.logging.LogUtils;
import kotlin.KotlinVersion;
import mods.eln.platform.ServerSimulationLifecycle;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/** Minimal NeoForge entry point for the clean Electrical Age port. */
@Mod(PortingBaseline.MOD_ID)
public final class ElectricalAge {
    public static final Logger LOGGER = LogUtils.getLogger();
    private final ServerSimulationLifecycle simulations = new ServerSimulationLifecycle();

    public ElectricalAge(IEventBus modBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(simulations::onServerStarting);
        NeoForge.EVENT_BUS.addListener(simulations::onServerTick);
        NeoForge.EVENT_BUS.addListener(simulations::onServerStopping);
        LOGGER.info(
                "Starting {} port for Minecraft {} on {} with Kotlin {}",
                modContainer.getModInfo().getDisplayName(),
                PortingBaseline.MINECRAFT_VERSION,
                PortingBaseline.MOD_LOADER,
                KotlinVersion.CURRENT);
    }
}
