package net.electricalage.eln.client;

import net.electricalage.eln.ElectricalAge;
import net.electricalage.eln.registry.ElnBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ElectricalAge.MOD_ID, value = Dist.CLIENT)
public final class ElectricalAgeClientEvents {
    private ElectricalAgeClientEvents() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ElnBlockEntities.SIX_NODE.get(),
                SixNodeBlockEntityRenderer::new
        );
    }
}
