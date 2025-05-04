package net.diemond_player.waxed_workstations;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

import static net.diemond_player.waxed_workstations.WaxedWorkstations.UNWAX_WORKSTATION_PACKET_ID;
import static net.diemond_player.waxed_workstations.WaxedWorkstations.WAX_WORKSTATION_PACKET_ID;

public class WaxedWorkstationsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(WAX_WORKSTATION_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            client.execute(() -> {
                client.world.syncWorldEvent(client.player, WorldEvents.BLOCK_WAXED, blockPos, 0);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(UNWAX_WORKSTATION_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();
            client.execute(() -> {
                client.world.playSound(client.player, blockPos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                client.world.syncWorldEvent(client.player, WorldEvents.WAX_REMOVED, blockPos, 0);
            });
        });
    }
}
