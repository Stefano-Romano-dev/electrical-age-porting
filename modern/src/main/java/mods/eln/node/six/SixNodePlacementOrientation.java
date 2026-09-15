package mods.eln.node.six;

import net.minecraft.core.Direction;

/** Legacy base-descriptor rotation chosen from the mounted face and player view. */
public final class SixNodePlacementOrientation {
    private SixNodePlacementOrientation() {}

    public static SixNodeRotation fromPlacement(Direction mountedFace, Direction playerHorizontalDirection) {
        if (mountedFace.getAxis().isHorizontal()) return SixNodeRotation.UP;

        return switch (mountedFace) {
            case DOWN -> switch (playerHorizontalDirection) {
                case EAST -> SixNodeRotation.UP;
                case WEST -> SixNodeRotation.DOWN;
                case NORTH -> SixNodeRotation.RIGHT;
                case SOUTH -> SixNodeRotation.LEFT;
                default -> throw new IllegalArgumentException("Player direction must be horizontal");
            };
            case UP -> switch (playerHorizontalDirection) {
                case EAST -> SixNodeRotation.DOWN;
                case WEST -> SixNodeRotation.UP;
                case NORTH -> SixNodeRotation.RIGHT;
                case SOUTH -> SixNodeRotation.LEFT;
                default -> throw new IllegalArgumentException("Player direction must be horizontal");
            };
            default -> throw new IllegalStateException("Unexpected mounted face: " + mountedFace);
        };
    }
}
