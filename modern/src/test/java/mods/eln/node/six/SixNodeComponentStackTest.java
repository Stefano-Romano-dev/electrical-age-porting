package mods.eln.node.six;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import mods.eln.registry.ElnContent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SixNodeComponentStackTest {
    @Test
    void typedStackUsesStableRegisteredItemAndComponentIds() {
        ItemStack stack = SixNodeComponentStack.create(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE);

        assertEquals("eln:six_node_component", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        assertEquals(
                "eln:six_node_component_type",
                BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(ElnContent.SIX_NODE_COMPONENT_TYPE.get()).toString());
        assertEquals(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeComponentStack.typeId(stack));
        assertEquals(
                SixNodeComponentCatalog.LOW_VOLTAGE_CABLE,
                SixNodeComponentStack.definition(stack, SixNodeComponentCatalog.FOUNDATION));
    }

    @Test
    void componentIdentitySurvivesItemStackSerialization() {
        ItemStack original = SixNodeComponentStack.create(SixNodeComponentCatalog.POWER_RESISTOR);
        Tag encoded = original.save(RegistryAccess.EMPTY);
        ItemStack restored = ItemStack.parse(RegistryAccess.EMPTY, encoded).orElseThrow();

        assertEquals(SixNodeComponentCatalog.POWER_RESISTOR.getId(), SixNodeComponentStack.typeId(restored));
    }

    @Test
    void unrelatedAndUnknownStacksAreHandledConservatively() {
        assertNull(SixNodeComponentStack.typeId(new ItemStack(Items.STICK)));

        ResourceLocation unknownId = ResourceLocation.fromNamespaceAndPath("eln", "future_component");
        ItemStack unknown = SixNodeComponentStack.create(unknownId);

        assertEquals(unknownId, SixNodeComponentStack.typeId(unknown));
        assertNull(SixNodeComponentStack.definition(unknown, SixNodeComponentCatalog.FOUNDATION));
    }
}
