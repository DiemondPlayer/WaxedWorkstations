package net.diemond_player.waxed_workstations.payload;

import net.diemond_player.waxed_workstations.WaxedWorkstationsClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UnwaxWorkstationPacket(BlockPos pos) implements CustomPayload {
    public static final Id<UnwaxWorkstationPacket> ID = new Id<>(WaxedWorkstationsClient.UNWAX_WORKSTATION_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, UnwaxWorkstationPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UnwaxWorkstationPacket::pos,
            UnwaxWorkstationPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
