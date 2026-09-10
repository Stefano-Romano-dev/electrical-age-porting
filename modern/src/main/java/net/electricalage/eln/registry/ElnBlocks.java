package net.electricalage.eln.registry;

import net.electricalage.eln.ElectricalAge;
import net.electricalage.eln.block.SixNodeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ElnBlocks {
    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ElectricalAge.MOD_ID);
    public static final DeferredBlock<SixNodeBlock> SIX_NODE = BLOCKS.register(
            "six_node",
            () -> new SixNodeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    private ElnBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
