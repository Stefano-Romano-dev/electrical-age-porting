package mods.eln.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

class SetElectricalSourceVoltagePayloadTest {
    @Test
    void typedPayloadRoundTripsTargetFaceAndLegacyFloatValue() {
        SetElectricalSourceVoltagePayload expected =
                new SetElectricalSourceVoltagePayload(new BlockPos(12, -60, 34), Direction.NORTH, 123.5F);
        RegistryFriendlyByteBuf buffer =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        SetElectricalSourceVoltagePayload.STREAM_CODEC.encode(buffer, expected);

        assertEquals(expected, SetElectricalSourceVoltagePayload.STREAM_CODEC.decode(buffer));
        buffer.release();
    }
}
