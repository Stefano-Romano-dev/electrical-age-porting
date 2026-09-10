package net.electricalage.eln.network;

import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mutable collection of loaded SixNodes for one server dimension. */
public final class WorldElectricalNetwork {
    private final Map<GridPosition, Map<MountFace, MountedComponent>> hosts = new LinkedHashMap<>();
    private ElectricalGraph graph = ElectricalGraphBuilder.build(Map.of());

    public void update(GridPosition position, Map<MountFace, MountedComponent> components) {
        Map<MountFace, MountedComponent> snapshot = Map.copyOf(components);
        if (snapshot.isEmpty()) {
            remove(position);
            return;
        }
        if (snapshot.equals(hosts.put(position, snapshot))) {
            return;
        }
        rebuild();
    }

    public void remove(GridPosition position) {
        if (hosts.remove(position) != null) {
            rebuild();
        }
    }

    public ElectricalGraph graph() {
        return graph;
    }

    public int hostCount() {
        return hosts.size();
    }

    private void rebuild() {
        Map<ComponentLocation, MountedComponent> flattened = new LinkedHashMap<>();
        hosts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(host -> host.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(component -> flattened.put(
                                new ComponentLocation(host.getKey(), component.getKey()),
                                component.getValue()
                        )));
        graph = ElectricalGraphBuilder.build(flattened);
    }
}
