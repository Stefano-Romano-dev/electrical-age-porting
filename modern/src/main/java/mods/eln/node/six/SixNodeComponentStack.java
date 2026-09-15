package mods.eln.node.six;

import mods.eln.registry.ElnContent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Typed stack boundary replacing the legacy SixNode ItemStack damage value. */
public final class SixNodeComponentStack {
    private SixNodeComponentStack() {}

    public static ItemStack create(SixNodeComponentDefinition definition) {
        return create(definition.getId());
    }

    public static ItemStack create(ResourceLocation typeId) {
        ItemStack stack = new ItemStack(ElnContent.SIX_NODE_COMPONENT.get());
        stack.set(ElnContent.SIX_NODE_COMPONENT_TYPE.get(), typeId);
        return stack;
    }

    public static ResourceLocation typeId(ItemStack stack) {
        if (!stack.is(ElnContent.SIX_NODE_COMPONENT.get())) return null;
        return stack.get(ElnContent.SIX_NODE_COMPONENT_TYPE.get());
    }

    public static SixNodeComponentDefinition definition(ItemStack stack, SixNodeComponentCatalog catalog) {
        ResourceLocation typeId = typeId(stack);
        return typeId == null ? null : catalog.get(typeId);
    }
}
