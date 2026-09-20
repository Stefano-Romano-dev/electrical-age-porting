package mods.eln.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class LegacyNumberFormatTest {
    @Test
    void parsesUsingThePlayersDecimalSeparator() {
        assertEquals(123.5, LegacyNumberFormat.parseFinite("123,5", Locale.ITALY).orElseThrow());
        assertEquals(123.5, LegacyNumberFormat.parseFinite("123.5", Locale.US).orElseThrow());
    }

    @Test
    void rejectsEmptyAndNonFiniteValues() {
        assertTrue(LegacyNumberFormat.parseFinite("", Locale.US).isEmpty());
        assertTrue(LegacyNumberFormat.parseFinite("NaN", Locale.US).isEmpty());
    }

    @Test
    void keepsLegacyDisplayPrecision() {
        assertEquals("12,35", LegacyNumberFormat.formatFloat(12.345F, Locale.ITALY).trim());
        assertEquals("1235", LegacyNumberFormat.formatFloat(1234.6F, Locale.ITALY).trim());
    }
}
