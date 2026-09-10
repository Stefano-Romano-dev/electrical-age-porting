package net.electricalage.eln.network;

import net.electricalage.eln.component.MountedComponent;

import java.util.Map;
import java.util.Set;

public record ElectricalGraph(
        Map<ComponentLocation, MountedComponent> components,
        Set<ElectricalLink> links,
        Map<TerminalAddress, Integer> terminalNets
) {
    public ElectricalGraph {
        components = Map.copyOf(components);
        links = Set.copyOf(links);
        terminalNets = Map.copyOf(terminalNets);
    }

    public int terminalCount() {
        return terminalNets.size();
    }

    public int netCount() {
        return (int) terminalNets.values().stream().distinct().count();
    }

    public int netOf(TerminalAddress terminal) {
        Integer net = terminalNets.get(terminal);
        if (net == null) {
            throw new IllegalArgumentException("Unknown terminal: " + terminal);
        }
        return net;
    }
}
