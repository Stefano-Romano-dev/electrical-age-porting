package mods.eln.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.junit.jupiter.api.Test;

class ServerSimulationLifecycleTest {
    @Test
    void lifecycleHandlersCreateTickAndRemoveTheServerOwner() {
        var lifecycle = new ServerSimulationLifecycle();

        lifecycle.onServerStarting(new ServerStartingEvent(null));
        assertEquals(1, lifecycle.size());
        assertNotNull(lifecycle.get(null));

        lifecycle.onServerTick(new ServerTickEvent.Pre(() -> true, null));
        lifecycle.onServerStopping(new ServerStoppingEvent(null));
        assertEquals(0, lifecycle.size());
    }
}
