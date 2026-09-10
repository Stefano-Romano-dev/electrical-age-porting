package net.electricalage.eln.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MountedComponentTest {
    @Test
    void localFramesMatchTheOriginalSixNodeConvention() {
        assertEquals(MountFace.UP, FaceOrientation.toWorld(MountFace.NORTH, LocalDirection.UP));
        assertEquals(MountFace.WEST, FaceOrientation.toWorld(MountFace.NORTH, LocalDirection.RIGHT));
        assertEquals(MountFace.NORTH, FaceOrientation.toWorld(MountFace.DOWN, LocalDirection.RIGHT));
        assertEquals(MountFace.SOUTH, FaceOrientation.toWorld(MountFace.UP, LocalDirection.RIGHT));
    }

    @Test
    void resistorHasTwoTerminalsOnOppositeEdges() {
        MountedComponent resistor = MountedComponent.resistor(LocalDirection.UP);

        assertEquals(MountFace.WEST, resistor.terminals(MountFace.NORTH).get(0).worldEdge());
        assertEquals(MountFace.EAST, resistor.terminals(MountFace.NORTH).get(1).worldEdge());
        assertEquals(
                resistor.terminals(MountFace.NORTH).get(0).localEdge().opposite(),
                resistor.terminals(MountFace.NORTH).get(1).localEdge()
        );
    }

    @Test
    void rejectsInvalidResistance() {
        assertThrows(IllegalArgumentException.class,
                () -> new MountedComponent(MountedComponentType.RESISTOR, LocalDirection.UP, 0.0, 0.0));
    }

    @Test
    void sourceAndCableExposeTwoTerminals() {
        assertEquals(2, MountedComponent.cable(LocalDirection.LEFT).terminals(MountFace.DOWN).size());
        assertEquals(2, MountedComponent.voltageSource(LocalDirection.LEFT).terminals(MountFace.DOWN).size());
        assertEquals(12.0, MountedComponent.voltageSource(LocalDirection.LEFT).voltageVolts());
    }
}
