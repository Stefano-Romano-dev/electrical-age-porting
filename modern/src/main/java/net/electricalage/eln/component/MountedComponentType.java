package net.electricalage.eln.component;

import java.util.Arrays;
import java.util.Optional;

public enum MountedComponentType {
    RESISTOR("resistor"),
    CABLE("cable"),
    VOLTAGE_SOURCE("voltage_source");

    private final String id;

    MountedComponentType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<MountedComponentType> fromId(String id) {
        return Arrays.stream(values()).filter(type -> type.id.equals(id)).findFirst();
    }
}
