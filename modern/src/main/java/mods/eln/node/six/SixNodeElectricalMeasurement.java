package mods.eln.node.six;

/** Immutable server-side reading exposed to Electrical Age measuring tools. */
public record SixNodeElectricalMeasurement(
        Kind kind, double voltage, double current, double resistance, double cablePowerLoss) {
    public enum Kind {
        CABLE,
        SOURCE,
        RESISTOR
    }
}
