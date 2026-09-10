package net.electricalage.eln.registry;

import net.electricalage.eln.ElectricalAge;
import net.electricalage.eln.blockentity.SixNodeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ElnBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ElectricalAge.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SixNodeBlockEntity>> SIX_NODE =
            BLOCK_ENTITIES.register("six_node", () ->
                    BlockEntityType.Builder.of(SixNodeBlockEntity::new, ElnBlocks.SIX_NODE.get()).build(null));

    private ElnBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
