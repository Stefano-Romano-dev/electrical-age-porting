package mods.eln.client;

import mods.eln.PortingBaseline;
import mods.eln.client.render.SixNodeBlockEntityRenderer;
import mods.eln.registry.ElnContent;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeComponentStack;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only registration boundary. No client class is referenced by common initialization. */
@EventBusSubscriber(modid = PortingBaseline.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ElnClient {
    private static final ResourceLocation SIX_NODE_TYPE_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(PortingBaseline.MOD_ID, "six_node_type");

    private ElnClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ElnContent.SIX_NODE_BLOCK_ENTITY.get(), SixNodeBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(SixNodeBlockEntityRenderer.ELECTRICAL_SOURCE_MODEL);
        event.register(SixNodeBlockEntityRenderer.POWER_RESISTOR_MODEL);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ElnContent.SIX_NODE_COMPONENT.get(),
                SIX_NODE_TYPE_PROPERTY,
                (stack, level, entity, seed) -> {
                    ResourceLocation type = SixNodeComponentStack.typeId(stack);
                    if (SixNodeComponentCatalog.ELECTRICAL_SOURCE.getId().equals(type)) return 1.0F;
                    if (SixNodeComponentCatalog.POWER_RESISTOR.getId().equals(type)) return 2.0F;
                    return 0.0F;
                }));
    }
}
