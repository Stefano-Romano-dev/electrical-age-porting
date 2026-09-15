package mods.eln.node.six;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

final class SixNodePlacementOrientationTest {
    @Test
    void lateralFacesUseLegacyUpRotationRegardlessOfPlayerView() {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            for (Direction view : Direction.Plane.HORIZONTAL) {
                assertEquals(SixNodeRotation.UP, SixNodePlacementOrientation.fromPlacement(face, view));
            }
        }
    }

    @Test
    void floorAndCeilingPreserveLegacyViewMapping() {
        assertEquals(SixNodeRotation.UP, SixNodePlacementOrientation.fromPlacement(Direction.DOWN, Direction.EAST));
        assertEquals(SixNodeRotation.DOWN, SixNodePlacementOrientation.fromPlacement(Direction.DOWN, Direction.WEST));
        assertEquals(SixNodeRotation.RIGHT, SixNodePlacementOrientation.fromPlacement(Direction.DOWN, Direction.NORTH));
        assertEquals(SixNodeRotation.LEFT, SixNodePlacementOrientation.fromPlacement(Direction.DOWN, Direction.SOUTH));

        assertEquals(SixNodeRotation.DOWN, SixNodePlacementOrientation.fromPlacement(Direction.UP, Direction.EAST));
        assertEquals(SixNodeRotation.UP, SixNodePlacementOrientation.fromPlacement(Direction.UP, Direction.WEST));
        assertEquals(SixNodeRotation.RIGHT, SixNodePlacementOrientation.fromPlacement(Direction.UP, Direction.NORTH));
        assertEquals(SixNodeRotation.LEFT, SixNodePlacementOrientation.fromPlacement(Direction.UP, Direction.SOUTH));
    }
}
