package net.electricalage.eln.component;

/** Converts directions on a component face to the corresponding block-space face. */
public final class FaceOrientation {
    private FaceOrientation() {
    }

    public static MountFace toWorld(MountFace support, LocalDirection local) {
        MountFace up = switch (support) {
            case DOWN, UP -> MountFace.EAST;
            case NORTH, SOUTH, WEST, EAST -> MountFace.UP;
        };
        MountFace right = switch (support) {
            case DOWN, EAST -> MountFace.NORTH;
            case UP, WEST -> MountFace.SOUTH;
            case NORTH -> MountFace.WEST;
            case SOUTH -> MountFace.EAST;
        };

        return switch (local) {
            case UP -> up;
            case RIGHT -> right;
            case DOWN -> up.opposite();
            case LEFT -> right.opposite();
        };
    }

    public static LocalDirection fromWorld(MountFace support, MountFace worldDirection) {
        for (LocalDirection local : LocalDirection.values()) {
            if (toWorld(support, local) == worldDirection) {
                return local;
            }
        }
        throw new IllegalArgumentException(worldDirection + " is perpendicular to face " + support);
    }
}
