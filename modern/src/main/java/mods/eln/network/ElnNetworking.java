package mods.eln.network;

import mods.eln.PortingBaseline;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PortingBaseline.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ElnNetworking {
    private ElnNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                SetElectricalSourceVoltagePayload.TYPE,
                SetElectricalSourceVoltagePayload.STREAM_CODEC,
                SetElectricalSourceVoltagePayload::handle);
    }
}
