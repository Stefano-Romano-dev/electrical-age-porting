package net.electricalage.eln.network;

import net.electricalage.eln.component.ElectricalTerminal;
import net.electricalage.eln.component.MountFace;
import net.electricalage.eln.component.MountedComponent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ElectricalGraphBuilder {
    private ElectricalGraphBuilder() {
    }

    public static ElectricalGraph build(Map<ComponentLocation, MountedComponent> components) {
        List<ComponentLocation> locations = components.keySet().stream().sorted().toList();
        Map<TerminalAddress, ElectricalTerminal> terminals = collectTerminals(components, locations);
        Set<ElectricalLink> links = new LinkedHashSet<>();

        for (ComponentLocation location : locations) {
            if (components.get(location).type() == net.electricalage.eln.component.MountedComponentType.CABLE) {
                links.add(new ElectricalLink(
                        new TerminalAddress(location, 0),
                        new TerminalAddress(location, 1)
                ));
            }
        }

        for (Map.Entry<TerminalAddress, ElectricalTerminal> entry : terminals.entrySet()) {
            TerminalAddress address = entry.getKey();
            MountFace edge = entry.getValue().worldEdge();
            ComponentLocation location = address.component();

            connectToMatchingTerminal(
                    address,
                    new ComponentLocation(location.position(), edge),
                    location.support(),
                    terminals,
                    components,
                    links
            );
            connectToMatchingTerminal(
                    address,
                    new ComponentLocation(location.position().offset(edge), location.support()),
                    edge.opposite(),
                    terminals,
                    components,
                    links
            );
        }

        return new ElectricalGraph(components, links, assignNets(terminals.keySet(), links));
    }

    private static Map<TerminalAddress, ElectricalTerminal> collectTerminals(
            Map<ComponentLocation, MountedComponent> components,
            List<ComponentLocation> locations
    ) {
        Map<TerminalAddress, ElectricalTerminal> terminals = new LinkedHashMap<>();
        for (ComponentLocation location : locations) {
            for (ElectricalTerminal terminal : components.get(location).terminals(location.support())) {
                terminals.put(new TerminalAddress(location, terminal.index()), terminal);
            }
        }
        return terminals;
    }

    private static void connectToMatchingTerminal(
            TerminalAddress source,
            ComponentLocation targetLocation,
            MountFace requiredEdge,
            Map<TerminalAddress, ElectricalTerminal> terminals,
            Map<ComponentLocation, MountedComponent> components,
            Set<ElectricalLink> links
    ) {
        MountedComponent target = components.get(targetLocation);
        if (target == null) {
            return;
        }
        for (ElectricalTerminal targetTerminal : target.terminals(targetLocation.support())) {
            if (targetTerminal.worldEdge() == requiredEdge) {
                TerminalAddress targetAddress = new TerminalAddress(targetLocation, targetTerminal.index());
                if (terminals.containsKey(targetAddress)) {
                    links.add(new ElectricalLink(source, targetAddress));
                }
            }
        }
    }

    private static Map<TerminalAddress, Integer> assignNets(
            Set<TerminalAddress> terminalSet,
            Set<ElectricalLink> links
    ) {
        List<TerminalAddress> terminals = terminalSet.stream().sorted().toList();
        Map<TerminalAddress, Integer> indexes = new LinkedHashMap<>();
        for (int index = 0; index < terminals.size(); index++) {
            indexes.put(terminals.get(index), index);
        }

        int[] parents = new int[terminals.size()];
        for (int index = 0; index < parents.length; index++) {
            parents[index] = index;
        }
        for (ElectricalLink link : links) {
            union(parents, indexes.get(link.first()), indexes.get(link.second()));
        }

        Map<Integer, Integer> rootNets = new LinkedHashMap<>();
        Map<TerminalAddress, Integer> nets = new LinkedHashMap<>();
        for (TerminalAddress terminal : terminals) {
            int root = find(parents, indexes.get(terminal));
            int net = rootNets.computeIfAbsent(root, ignored -> rootNets.size());
            nets.put(terminal, net);
        }
        return nets;
    }

    private static int find(int[] parents, int value) {
        if (parents[value] != value) {
            parents[value] = find(parents, parents[value]);
        }
        return parents[value];
    }

    private static void union(int[] parents, int first, int second) {
        int firstRoot = find(parents, first);
        int secondRoot = find(parents, second);
        if (firstRoot != secondRoot) {
            parents[Math.max(firstRoot, secondRoot)] = Math.min(firstRoot, secondRoot);
        }
    }
}
