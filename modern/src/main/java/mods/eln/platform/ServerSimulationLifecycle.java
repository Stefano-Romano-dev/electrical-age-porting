package mods.eln.platform;

import java.util.IdentityHashMap;
import java.util.Map;
import mods.eln.ElectricalAge;
import mods.eln.sim.Simulator;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Owns one simulation context for each concrete Minecraft server instance. */
public final class ServerSimulationLifecycle {
    private static final double CALL_PERIOD = 1.0 / 20.0;
    private static final double ELECTRICAL_PERIOD = 1.0 / 20.0;
    private static final int ELECTRICAL_INTER_SYSTEM_OVERSAMPLING = 50;
    private static final double THERMAL_PERIOD = 1.0 / 400.0;

    private final Map<MinecraftServer, Simulator> simulations = new IdentityHashMap<>();

    public void onServerStarting(ServerStartingEvent event) {
        var simulator = new Simulator(
                CALL_PERIOD,
                ELECTRICAL_PERIOD,
                ELECTRICAL_INTER_SYSTEM_OVERSAMPLING,
                THERMAL_PERIOD,
                null);
        simulations.put(event.getServer(), simulator);
        ElectricalAge.LOGGER.info("Created Electrical Age simulation owner for server {}", event.getServer());
    }

    public void onServerTick(ServerTickEvent.Pre event) {
        var simulator = simulations.get(event.getServer());
        if (simulator != null) {
            simulator.tick();
        }
    }

    public void onServerStopping(ServerStoppingEvent event) {
        if (simulations.remove(event.getServer()) != null) {
            ElectricalAge.LOGGER.info("Removed Electrical Age simulation owner for server {}", event.getServer());
        }
    }

    public Simulator get(MinecraftServer server) {
        return simulations.get(server);
    }

    public int size() {
        return simulations.size();
    }
}
