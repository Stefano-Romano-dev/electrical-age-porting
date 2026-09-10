package net.electricalage.eln.simulation;

import java.util.Objects;

/** A voltage node in a DC circuit. Ground is represented by {@link #GROUND}. */
public record CircuitNode(String id) {
    public static final CircuitNode GROUND = new CircuitNode("ground");

    public CircuitNode {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("A circuit node id cannot be blank");
        }
    }
}
