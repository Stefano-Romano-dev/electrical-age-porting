package mods.eln.node.six;

import java.util.Locale;

/** Exact engineering-unit presentation used by the 1.24.8 multimeter strings. */
public final class LegacyElectricalMeasurementFormatter {
    private LegacyElectricalMeasurementFormatter() {}

    public static String format(SixNodeElectricalMeasurement measurement) {
        String electrical = plotUIP(measurement.voltage(), measurement.current());
        return switch (measurement.kind()) {
            case SOURCE -> electrical;
            case CABLE -> electrical + " " + plotPower("Cable Power Loss", measurement.cablePowerLoss());
            case RESISTOR -> plotOhm(electrical, measurement.resistance());
        };
    }

    public static String plotValue(double value) {
        double absolute = Math.abs(value);
        if (absolute < 0.0001) return "0";
        if (absolute < 0.000999) return formatted("%1.2fµ", value * 10_000.0);
        if (absolute < 0.00999) return formatted("%1.2fm", value * 1_000.0);
        if (absolute < 0.0999) return formatted("%2.1fm", value * 1_000.0);
        if (absolute < 0.999) return formatted("%3.0fm", value * 1_000.0);
        if (absolute < 9.99) return formatted("%1.2f", value);
        if (absolute < 99.9) return formatted("%2.1f", value);
        if (absolute < 999) return formatted("%3.0f", value);
        if (absolute < 9_999) return formatted("%1.2fk", value / 1_000.0);
        if (absolute < 99_999) return formatted("%2.1fk", value / 1_000.0);
        if (absolute < 999_999) return formatted("%3.0fK", value / 1_000.0);
        if (absolute < 9_999_999) return formatted("%1.2fM", value / 1_000_000.0);
        if (absolute < 99_999_999) return formatted("%2.1fM", value / 1_000_000.0);
        if (absolute < 999_999_999) return formatted("%3.0fM", value / 1_000_000.0);
        if (absolute < 9_999_999_999.0) return formatted("%1.2fG", value / 1_000_000_000.0);
        if (absolute < 99_999_999_999.0) return formatted("%2.1fG", value / 1_000_000_000.0);
        if (absolute < 999_999_999_999.0) return formatted("%3.0fG", value / 1_000_000_000.0);
        if (absolute < 9_999_999_999_999.0) return formatted("%1.2fT", value / 1_000_000_000_000.0);
        if (absolute < 99_999_999_999_999.0) return formatted("%2.1fT", value / 1_000_000_000_000.0);
        if (absolute < 999_999_999_999_999.0) return formatted("%3.0fT", value / 1_000_000_000_000.0);
        if (absolute < 9_999_999_999_999_999.0) return formatted("%1.2fP", value / 1_000_000_000_000_000.0);
        if (absolute < 99_999_999_999_999_999.0) return formatted("%2.1fP", value / 1_000_000_000_000_000.0);
        if (absolute < 999_999_999_999_999_999.0) return formatted("%3.0fP", value / 1_000_000_000_000_000.0);
        if (absolute < 9_999_999_999_999_999_999.0) return formatted("%1.2fE", value / 1_000_000_000_000_000_000.0);
        if (absolute < 99_999_999_999_999_999_999.0) return formatted("%2.1fE", value / 1_000_000_000_000_000_000.0);
        return formatted("%3.0fE", value / 1_000_000_000_000_000_000.0);
    }

    private static String plotUIP(double voltage, double current) {
        return withHeader("U", plotValue(voltage) + "V  ")
                + withHeader("I", plotValue(current) + "A  ")
                + plotPower("P", Math.abs(voltage * current));
    }

    private static String plotPower(String header, double value) {
        return withHeader(header, plotValue(value) + "W  ");
    }

    private static String plotOhm(String header, double value) {
        return withHeader(header, plotValue(value) + "Ω ");
    }

    private static String withHeader(String header, String value) {
        return header.isEmpty() ? value : header + " " + value;
    }

    private static String formatted(String pattern, double value) {
        return String.format(Locale.getDefault(), pattern, value);
    }
}
