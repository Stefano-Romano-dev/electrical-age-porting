package mods.eln.registry;

import mods.eln.PortingBaseline;
import mods.eln.node.six.SixNodeBlock;
import mods.eln.node.six.SixNodeBlockEntity;
import mods.eln.node.six.SixNodeComponentItem;
import mods.eln.node.six.SixNodeComponentCatalog;
import mods.eln.node.six.SixNodeComponentStack;
import mods.eln.item.MultimeterItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import mods.eln.menu.ElectricalSourceMenu;

/** NeoForge registry boundary for the modern port. */
public final class ElnContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PortingBaseline.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PortingBaseline.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PortingBaseline.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PortingBaseline.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, PortingBaseline.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>>
            SIX_NODE_COMPONENT_TYPE = DATA_COMPONENT_TYPES.registerComponentType(
                    "six_node_component_type",
                    builder -> builder.persistent(ResourceLocation.CODEC)
                            .networkSynchronized(ResourceLocation.STREAM_CODEC));

    public static final DeferredBlock<SixNodeBlock> SIX_NODE = BLOCKS.registerBlock(
            "six_node", SixNodeBlock::new, BlockBehaviour.Properties.of().noCollission().noOcclusion());

    public static final DeferredItem<Item> SIX_NODE_COMPONENT =
            ITEMS.registerItem("six_node_component", SixNodeComponentItem::new);

    /** Legacy shared-item id 896 (14 << 6), now represented by a stable namespaced item. */
    public static final DeferredItem<Item> MULTIMETER = ITEMS.registerItem("multimeter", MultimeterItem::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SixNodeBlockEntity>> SIX_NODE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "six_node",
                    () -> BlockEntityType.Builder.of(SixNodeBlockEntity::new, SIX_NODE.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricalSourceMenu>> ELECTRICAL_SOURCE_MENU =
            MENU_TYPES.register("electrical_source", () -> IMenuTypeExtension.create(ElectricalSourceMenu::fromNetwork));

    private ElnContent() {}

    public static void register(IEventBus modBus) {
        DATA_COMPONENT_TYPES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        modBus.addListener(ElnContent::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.REDSTONE_BLOCKS)) return;
        event.accept(SixNodeComponentStack.create(SixNodeComponentCatalog.LOW_VOLTAGE_CABLE));
        event.accept(SixNodeComponentStack.create(SixNodeComponentCatalog.ELECTRICAL_SOURCE));
        event.accept(SixNodeComponentStack.create(SixNodeComponentCatalog.POWER_RESISTOR));
        event.accept(MULTIMETER.get());
    }
}
