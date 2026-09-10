package net.electricalage.eln.network;

import java.util.Objects;

public record TerminalAddress(ComponentLocation component, int terminalIndex) implements Comparable<TerminalAddress> {
    public TerminalAddress {
        Objects.requireNonNull(component, "component");
        if (terminalIndex < 0) {
            throw new IllegalArgumentException("Terminal index cannot be negative");
        }
    }

    @Override
    public int compareTo(TerminalAddress other) {
        int componentOrder = component.compareTo(other.component);
        return componentOrder != 0 ? componentOrder : Integer.compare(terminalIndex, other.terminalIndex);
    }
}
