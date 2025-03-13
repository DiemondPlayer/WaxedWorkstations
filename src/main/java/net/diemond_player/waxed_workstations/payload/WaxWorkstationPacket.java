package net.diemond_player.waxed_workstations.payload;

import net.diemond_player.waxed_workstations.WaxedWorkstationsClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record WaxWorkstationPacket(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<WaxWorkstationPacket> ID = new CustomPayload.Id<>(WaxedWorkstationsClient.WAX_WORKSTATION_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, WaxWorkstationPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, WaxWorkstationPacket::pos,
            WaxWorkstationPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
