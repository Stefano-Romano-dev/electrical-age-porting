package net.electricalage.eln.network;

import net.electricalage.eln.component.MountFace;

/** Integer block position kept independent from Minecraft's BlockPos. */
public record GridPosition(int x, int y, int z) implements Comparable<GridPosition> {
    public GridPosition offset(MountFace direction) {
        return switch (direction) {
            case DOWN -> new GridPosition(x, y - 1, z);
            case UP -> new GridPosition(x, y + 1, z);
            case NORTH -> new GridPosition(x, y, z - 1);
            case SOUTH -> new GridPosition(x, y, z + 1);
            case WEST -> new GridPosition(x - 1, y, z);
            case EAST -> new GridPosition(x + 1, y, z);
        };
    }

    @Override
    public int compareTo(GridPosition other) {
        int xOrder = Integer.compare(x, other.x);
        if (xOrder != 0) return xOrder;
        int yOrder = Integer.compare(y, other.y);
        if (yOrder != 0) return yOrder;
        return Integer.compare(z, other.z);
    }
}
