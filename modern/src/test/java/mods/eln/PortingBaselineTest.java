package mods.eln;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PortingBaselineTest {
    @Test
    void pinsTheAgreedPlatformIdentity() {
        assertEquals("eln", PortingBaseline.MOD_ID);
        assertEquals("1.21.1", PortingBaseline.MINECRAFT_VERSION);
        assertEquals("NeoForge", PortingBaseline.MOD_LOADER);
        assertEquals(21, PortingBaseline.JAVA_VERSION);
    }
}
