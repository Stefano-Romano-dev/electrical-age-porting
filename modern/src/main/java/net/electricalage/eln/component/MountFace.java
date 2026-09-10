package net.electricalage.eln.component;

/** One of the six faces of the block that can hold a component. */
public enum MountFace {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    public MountFace opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }
}
