package mods.eln.node.six;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyElectricalMeasurementFormatterTest {
    private Locale previousLocale;

    @BeforeEach
    void useStableLocale() {
        previousLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(previousLocale);
    }

    @Test
    void preservesLegacyEngineeringThresholdsIncludingMicroScale() {
        assertEquals("5.00µ", LegacyElectricalMeasurementFormatter.plotValue(0.0005));
        assertEquals("5.00m", LegacyElectricalMeasurementFormatter.plotValue(0.005));
        assertEquals("5.00k", LegacyElectricalMeasurementFormatter.plotValue(5000.0));
    }

    @Test
    void formatsTheThreeInitialSixNodeRuntimeKinds() {
        assertEquals(
                "U 50.0V  I 2.00A  P 100W  ",
                LegacyElectricalMeasurementFormatter.format(new SixNodeElectricalMeasurement(
                        SixNodeElectricalMeasurement.Kind.SOURCE, 50.0, 2.0, Double.NaN, Double.NaN)));
        assertEquals(
                "U 12.0V  I 2.00A  P 24.0W   Cable Power Loss 50.0mW  ",
                LegacyElectricalMeasurementFormatter.format(new SixNodeElectricalMeasurement(
                        SixNodeElectricalMeasurement.Kind.CABLE, 12.0, 2.0, Double.NaN, 0.05)));
        assertEquals(
                "U -5.00V  I 500mA  P 2.50W   10.0Ω ",
                LegacyElectricalMeasurementFormatter.format(new SixNodeElectricalMeasurement(
                        SixNodeElectricalMeasurement.Kind.RESISTOR, -5.0, 0.5, 10.0, Double.NaN)));
    }
}
