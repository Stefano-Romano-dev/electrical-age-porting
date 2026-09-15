package mods.eln.node.six;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class SixNodeBreakSelectionTest {
    private static final BlockPos POS = BlockPos.ZERO;

    @Test
    void directedLegacyRaySelectsTheExitFace() {
        Map<Direction, MountedSixNodeComponent> contents = contents(Direction.WEST, Direction.EAST);

        assertEquals(
                Direction.EAST,
                SixNodeBreakSelection.resolve(POS, new Vec3(-2.0, 0.5, 0.5), new Vec3(1.0, 0.0, 0.0), contents));
        assertEquals(
                Direction.WEST,
                SixNodeBreakSelection.resolve(POS, new Vec3(2.0, 0.5, 0.5), new Vec3(-1.0, 0.0, 0.0), contents));
    }

    @Test
    void missingRayFallsBackToOccupiedFaceMostOpposedToLook() {
        Map<Direction, MountedSixNodeComponent> contents = contents(Direction.WEST, Direction.NORTH);

        assertEquals(
                Direction.NORTH,
                SixNodeBreakSelection.resolve(POS, new Vec3(5.0, 5.0, 5.0), new Vec3(0.0, 0.0, 1.0), contents));
    }

    private static Map<Direction, MountedSixNodeComponent> contents(Direction... faces) {
        Map<Direction, MountedSixNodeComponent> result = new EnumMap<>(Direction.class);
        for (Direction face : faces) {
            result.put(
                    face,
                    new MountedSixNodeComponent(
                            SixNodeComponentCatalog.LOW_VOLTAGE_CABLE.getId(), SixNodeRotation.UP));
        }
        return result;
    }
}
