package net.electricalage.eln.network;

import net.electricalage.eln.component.LocalDirection;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldElectricalNetworkTest {
    @Test
    void rebuildsWhenHostsAreAddedUpdatedAndRemoved() {
        WorldElectricalNetwork network = new WorldElectricalNetwork();
        GridPosition origin = new GridPosition(4, 8, 12);
        GridPosition north = origin.offset(MountFace.NORTH);

        network.update(origin, Map.of(MountFace.DOWN, MountedComponent.resistor(LocalDirection.UP)));
        assertEquals(1, network.hostCount());
        assertEquals(1, network.graph().components().size());
        assertEquals(0, network.graph().links().size());

        network.update(north, Map.of(MountFace.DOWN, MountedComponent.resistor(LocalDirection.UP)));
        assertEquals(2, network.hostCount());
        assertEquals(2, network.graph().components().size());
        assertEquals(1, network.graph().links().size());

        network.update(north, Map.of(MountFace.DOWN, MountedComponent.resistor(LocalDirection.RIGHT)));
        assertEquals(0, network.graph().links().size());

        network.remove(north);
        assertEquals(1, network.hostCount());
        assertEquals(1, network.graph().components().size());

        network.update(origin, Map.of());
        assertEquals(0, network.hostCount());
        assertEquals(0, network.graph().components().size());
    }
}
