package net.electricalage.eln.component;

/** One electrical connection point on an edge of a mounted component. */
public record ElectricalTerminal(int index, LocalDirection localEdge, MountFace worldEdge) {
}
