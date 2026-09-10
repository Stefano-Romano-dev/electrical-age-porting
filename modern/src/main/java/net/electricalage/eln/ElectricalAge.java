package net.electricalage.eln;

import com.mojang.logging.LogUtils;
import net.electricalage.eln.registry.ElnBlocks;
import net.electricalage.eln.registry.ElnBlockEntities;
import net.electricalage.eln.registry.ElnItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ElectricalAge.MOD_ID)
public final class ElectricalAge {
    public static final String MOD_ID = "eln";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.eln.main"))
                    .icon(() -> ElnItems.RESISTOR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ElnItems.RESISTOR.get());
                        output.accept(ElnItems.CABLE.get());
                        output.accept(ElnItems.VOLTAGE_SOURCE.get());
                    })
                    .build());

    public ElectricalAge(IEventBus modBus) {
        ElnBlocks.register(modBus);
        ElnItems.register(modBus);
        ElnBlockEntities.register(modBus);
        CREATIVE_TABS.register(modBus);
        LOGGER.info("Electrical Age minimal port initialized");
    }
}
