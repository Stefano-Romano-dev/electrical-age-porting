package mods.eln.node.six;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mods.eln.registry.ElnContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SixNodeBlockEntityPersistenceTest {
    @Test
    void blockEntityRoundTripsAllOwnedFaceState() {
        SixNodeBlock block = ElnContent.SIX_NODE.get();
        assertEquals("eln:six_node", BuiltInRegistries.BLOCK.getKey(block).toString());
        assertEquals(
                "eln:six_node",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(ElnContent.SIX_NODE_BLOCK_ENTITY.get()).toString());
        SixNodeBlockEntity original = new SixNodeBlockEntity(BlockPos.ZERO, block.defaultBlockState());

        assertTrue(original.mount(
                Direction.WEST,
                new MountedSixNodeComponent(
                        ResourceLocation.fromNamespaceAndPath("eln", "low_voltage_cable"), SixNodeRotation.RIGHT)));
        assertTrue(original.mount(
                Direction.UP,
                new MountedSixNodeComponent(
                        ResourceLocation.fromNamespaceAndPath("eln", "power_resistor"), SixNodeRotation.DOWN)));
        assertFalse(original.mount(
                Direction.WEST,
                new MountedSixNodeComponent(
                        ResourceLocation.fromNamespaceAndPath("eln", "electrical_source"), SixNodeRotation.LEFT)));

        CompoundTag saved = new CompoundTag();
        original.saveAdditional(saved, RegistryAccess.EMPTY);

        SixNodeBlockEntity restored = new SixNodeBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        restored.loadAdditional(saved, RegistryAccess.EMPTY);

        assertEquals(original.contentsSnapshot(), restored.contentsSnapshot());
    }

    @Test
    void unsupportedFutureSchemaDoesNotReplaceCurrentWorldState() {
        SixNodeBlock block = ElnContent.SIX_NODE.get();
        SixNodeBlockEntity entity = new SixNodeBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        MountedSixNodeComponent component = new MountedSixNodeComponent(
                ResourceLocation.fromNamespaceAndPath("eln", "electrical_source"), SixNodeRotation.UP);
        assertTrue(entity.mount(Direction.NORTH, component));

        CompoundTag future = new CompoundTag();
        future.putInt("formatVersion", 2);
        entity.loadAdditional(future, RegistryAccess.EMPTY);

        assertEquals(component, entity.contentsSnapshot().get(Direction.NORTH));
    }
}
