package mods.eln;

import com.mojang.logging.LogUtils;
import kotlin.KotlinVersion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** Minimal NeoForge entry point for the clean Electrical Age port. */
@Mod(PortingBaseline.MOD_ID)
public final class ElectricalAge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElectricalAge(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info(
                "Starting {} port for Minecraft {} on {} with Kotlin {}",
                modContainer.getModInfo().getDisplayName(),
                PortingBaseline.MINECRAFT_VERSION,
                PortingBaseline.MOD_LOADER,
                KotlinVersion.CURRENT);
    }
}
