package net.electricalage.eln.registry;

import net.electricalage.eln.ElectricalAge;
import net.electricalage.eln.item.ResistorItem;
import net.electricalage.eln.item.MountedComponentItem;
import net.electricalage.eln.component.MountedComponent;
import net.electricalage.eln.component.MountedComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ElnItems {
    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ElectricalAge.MOD_ID);

    public static final DeferredItem<ResistorItem> RESISTOR =
            ITEMS.register("resistor", () -> new ResistorItem(new Item.Properties()));
    public static final DeferredItem<MountedComponentItem> CABLE =
            ITEMS.register("cable", () -> new MountedComponentItem(new Item.Properties(), MountedComponent::cable));
    public static final DeferredItem<MountedComponentItem> VOLTAGE_SOURCE =
            ITEMS.register("voltage_source", () -> new MountedComponentItem(
                    new Item.Properties(), MountedComponent::voltageSource));

    private ElnItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static Item itemFor(MountedComponentType type) {
        return switch (type) {
            case RESISTOR -> RESISTOR.get();
            case CABLE -> CABLE.get();
            case VOLTAGE_SOURCE -> VOLTAGE_SOURCE.get();
        };
    }
}
