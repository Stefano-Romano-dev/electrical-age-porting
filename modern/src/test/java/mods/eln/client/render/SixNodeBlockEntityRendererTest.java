package mods.eln.client.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mods.eln.node.six.SixNodeRotation;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class SixNodeBlockEntityRendererTest {
    @Test
    void legacyNodeCapIsHiddenOnlyOnStraightTwoWayCable() {
        assertTrue(SixNodeBlockEntityRenderer.drawNode(List.of()));
        assertTrue(SixNodeBlockEntityRenderer.drawNode(List.of(SixNodeRotation.LEFT)));
        assertFalse(SixNodeBlockEntityRenderer.drawNode(
                List.of(SixNodeRotation.LEFT, SixNodeRotation.RIGHT)));
        assertFalse(SixNodeBlockEntityRenderer.drawNode(
                List.of(SixNodeRotation.DOWN, SixNodeRotation.UP)));
        assertTrue(SixNodeBlockEntityRenderer.drawNode(
                List.of(SixNodeRotation.LEFT, SixNodeRotation.UP)));
        assertTrue(SixNodeBlockEntityRenderer.drawNode(List.of(
                SixNodeRotation.LEFT,
                SixNodeRotation.RIGHT,
                SixNodeRotation.DOWN)));
    }

    @Test
    void legacyCornerChoosesExactlyOneArmForOverlap() {
        float normal = 0.5F;
        float cableHeight = 0.95F / 16.0F;

        assertEquals(
                normal + cableHeight,
                SixNodeBlockEntityRenderer.legacyOuterCornerArmLength(Direction.DOWN, Direction.NORTH));
        assertEquals(
                normal,
                SixNodeBlockEntityRenderer.legacyOuterCornerArmLength(Direction.SOUTH, Direction.UP));
        assertEquals(
                normal - cableHeight,
                SixNodeBlockEntityRenderer.legacyInternalArmLength(Direction.NORTH, Direction.DOWN));
        assertEquals(
                normal,
                SixNodeBlockEntityRenderer.legacyInternalArmLength(Direction.WEST, Direction.UP));
    }
}
