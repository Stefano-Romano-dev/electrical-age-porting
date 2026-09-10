package net.electricalage.eln.item;

import net.electricalage.eln.component.MountedComponent;
import net.minecraft.world.item.Item;

public final class ResistorItem extends MountedComponentItem {
    public ResistorItem(Properties properties) {
        super(properties, MountedComponent::resistor);
    }
}
