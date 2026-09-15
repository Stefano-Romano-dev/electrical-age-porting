package mods.eln;

import com.mojang.logging.LogUtils;
import kotlin.KotlinVersion;
import mods.eln.gametest.SixNodeDiskPersistenceProbe;
import mods.eln.platform.ServerSimulationLifecycle;
import mods.eln.registry.ElnContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/** Minimal NeoForge entry point for the clean Electrical Age port. */
@Mod(PortingBaseline.MOD_ID)
public final class ElectricalAge {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ServerSimulationLifecycle simulations;
    private final SixNodeDiskPersistenceProbe sixNodeDiskPersistenceProbe = new SixNodeDiskPersistenceProbe();

    public ElectricalAge(IEventBus modBus, ModContainer modContainer) {
        simulations = new ServerSimulationLifecycle();
        ElnContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(simulations::onServerStarting);
        NeoForge.EVENT_BUS.addListener(simulations::onServerTick);
        NeoForge.EVENT_BUS.addListener(simulations::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(simulations::onServerStopping);
        NeoForge.EVENT_BUS.addListener(simulations::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(simulations::onChunkUnload);
        if (!FMLEnvironment.production) {
            NeoForge.EVENT_BUS.addListener(sixNodeDiskPersistenceProbe::onServerStarted);
            NeoForge.EVENT_BUS.addListener(sixNodeDiskPersistenceProbe::onServerTick);
        }
        LOGGER.info(
                "Starting {} port for Minecraft {} on {} with Kotlin {}",
                modContainer.getModInfo().getDisplayName(),
                PortingBaseline.MINECRAFT_VERSION,
                PortingBaseline.MOD_LOADER,
                KotlinVersion.CURRENT);
    }

    public static ServerSimulationLifecycle simulationLifecycle() {
        return simulations;
    }
}
