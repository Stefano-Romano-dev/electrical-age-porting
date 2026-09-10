package net.electricalage.eln.component;

import java.util.List;

public record MountedComponent(
        MountedComponentType type,
        LocalDirection front,
        double resistanceOhms,
        double voltageVolts
) {
    public static final double DEFAULT_RESISTANCE_OHMS = 100.0;
    public static final double DEFAULT_SOURCE_VOLTS = 12.0;

    public MountedComponent {
        if (type == null || front == null) {
            throw new NullPointerException("Component type and front are required");
        }
        if (!Double.isFinite(resistanceOhms) || !Double.isFinite(voltageVolts)) {
            throw new IllegalArgumentException("Electrical values must be finite");
        }
        if (type == MountedComponentType.RESISTOR && resistanceOhms <= 0.0) {
            throw new IllegalArgumentException("Resistance must be greater than zero");
        }
    }

    public static MountedComponent resistor(LocalDirection front) {
        return new MountedComponent(MountedComponentType.RESISTOR, front, DEFAULT_RESISTANCE_OHMS, 0.0);
    }

    public static MountedComponent cable(LocalDirection front) {
        return new MountedComponent(MountedComponentType.CABLE, front, 0.0, 0.0);
    }

    public static MountedComponent voltageSource(LocalDirection front) {
        return new MountedComponent(MountedComponentType.VOLTAGE_SOURCE, front, 0.0, DEFAULT_SOURCE_VOLTS);
    }

    public List<ElectricalTerminal> terminals(MountFace support) {
        LocalDirection firstEdge = front.clockwise();
        LocalDirection secondEdge = front.counterClockwise();
        return List.of(
                new ElectricalTerminal(0, firstEdge, FaceOrientation.toWorld(support, firstEdge)),
                new ElectricalTerminal(1, secondEdge, FaceOrientation.toWorld(support, secondEdge))
        );
    }
}
