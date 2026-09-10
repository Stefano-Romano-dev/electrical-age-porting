package net.electricalage.eln.network;

import java.util.Objects;

/** A zero-resistance contact between two component terminals. */
public record ElectricalLink(TerminalAddress first, TerminalAddress second) implements Comparable<ElectricalLink> {
    public ElectricalLink {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.equals(second)) {
            throw new IllegalArgumentException("A terminal cannot be linked to itself");
        }
        if (first.compareTo(second) > 0) {
            TerminalAddress temporary = first;
            first = second;
            second = temporary;
        }
    }

    @Override
    public int compareTo(ElectricalLink other) {
        int firstOrder = first.compareTo(other.first);
        return firstOrder != 0 ? firstOrder : second.compareTo(other.second);
    }
}
