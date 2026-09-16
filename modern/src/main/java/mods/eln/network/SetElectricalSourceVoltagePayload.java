package mods.eln.network;

import mods.eln.PortingBaseline;
import mods.eln.menu.ElectricalSourceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Legacy set-voltage command represented as a typed, server-bound play payload. */
public record SetElectricalSourceVoltagePayload(BlockPos hostPos, Direction face, float voltage)
        implements CustomPacketPayload {
    public static final Type<SetElectricalSourceVoltagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PortingBaseline.MOD_ID, "set_electrical_source_voltage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetElectricalSourceVoltagePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBlockPos(payload.hostPos());
                        buffer.writeEnum(payload.face());
                        buffer.writeFloat(payload.voltage());
                    },
                    buffer -> new SetElectricalSourceVoltagePayload(
                            buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetElectricalSourceVoltagePayload payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof ElectricalSourceMenu menu)) return;
        if (!menu.hostPos().equals(payload.hostPos()) || menu.face() != payload.face()) return;
        menu.applyVoltage(context.player(), payload.voltage());
    }
}
