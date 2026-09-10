package net.electricalage.eln.network;

import net.electricalage.eln.component.MountFace;

import java.util.Objects;

public record ComponentLocation(GridPosition position, MountFace support) implements Comparable<ComponentLocation> {
    public ComponentLocation {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(support, "support");
    }

    @Override
    public int compareTo(ComponentLocation other) {
        int positionOrder = position.compareTo(other.position);
        return positionOrder != 0 ? positionOrder : support.compareTo(other.support);
    }
}
