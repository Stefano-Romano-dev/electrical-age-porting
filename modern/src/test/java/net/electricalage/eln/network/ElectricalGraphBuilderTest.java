package net.electricalage.eln.network;

import net.electricalage.eln.component.LocalDirection;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElectricalGraphBuilderTest {
    private static final GridPosition ORIGIN = new GridPosition(0, 0, 0);

    @Test
    void connectsCoplanarComponentsAcrossNeighboringBlocks() {
        ComponentLocation first = new ComponentLocation(ORIGIN, MountFace.DOWN);
        ComponentLocation second = new ComponentLocation(ORIGIN.offset(MountFace.NORTH), MountFace.DOWN);

        ElectricalGraph graph = ElectricalGraphBuilder.build(Map.of(
                first, MountedComponent.resistor(LocalDirection.UP),
                second, MountedComponent.resistor(LocalDirection.UP)
        ));

        assertEquals(1, graph.links().size());
        assertEquals(4, graph.terminalCount());
        assertEquals(3, graph.netCount());
        assertEquals(graph.netOf(new TerminalAddress(first, 0)), graph.netOf(new TerminalAddress(second, 1)));
    }

    @Test
    void connectsComponentsAroundAnInternalSixNodeCorner() {
        ComponentLocation north = new ComponentLocation(ORIGIN, MountFace.NORTH);
        ComponentLocation west = new ComponentLocation(ORIGIN, MountFace.WEST);

        ElectricalGraph graph = ElectricalGraphBuilder.build(Map.of(
                north, MountedComponent.resistor(LocalDirection.UP),
                west, MountedComponent.resistor(LocalDirection.UP)
        ));

        assertEquals(1, graph.links().size());
        assertEquals(graph.netOf(new TerminalAddress(north, 0)), graph.netOf(new TerminalAddress(west, 1)));
    }

    @Test
    void leavesNonFacingTerminalsDisconnected() {
        ComponentLocation north = new ComponentLocation(ORIGIN, MountFace.NORTH);
        ComponentLocation west = new ComponentLocation(ORIGIN, MountFace.WEST);

        ElectricalGraph graph = ElectricalGraphBuilder.build(Map.of(
                north, MountedComponent.resistor(LocalDirection.UP),
                west, MountedComponent.resistor(LocalDirection.RIGHT)
        ));

        assertEquals(0, graph.links().size());
        assertEquals(4, graph.netCount());
    }

    @Test
    void cableJoinsItsTwoTerminalsIntoOneNet() {
        ComponentLocation cable = new ComponentLocation(ORIGIN, MountFace.DOWN);
        ElectricalGraph graph = ElectricalGraphBuilder.build(Map.of(
                cable, MountedComponent.cable(LocalDirection.LEFT)
        ));

        assertEquals(1, graph.links().size());
        assertEquals(1, graph.netCount());
        assertEquals(graph.netOf(new TerminalAddress(cable, 0)), graph.netOf(new TerminalAddress(cable, 1)));
    }
}
