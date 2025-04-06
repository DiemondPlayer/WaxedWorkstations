package net.diemond_player.waxed_workstations;

import net.diemond_player.waxed_workstations.payload.UnwaxWorkstationPacket;
import net.diemond_player.waxed_workstations.payload.WaxWorkstationPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;


public class WaxedWorkstationsClient implements ClientModInitializer {
    public static final Identifier WAX_WORKSTATION_PACKET_ID = Identifier.of(WaxedWorkstations.MOD_ID, "wax_workstation");
    public static final Identifier UNWAX_WORKSTATION_PACKET_ID = Identifier.of(WaxedWorkstations.MOD_ID, "unwax_workstation");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UnwaxWorkstationPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().world.playSound(context.client().player, payload.pos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                context.client().world.syncWorldEvent(context.client().player, WorldEvents.WAX_REMOVED, payload.pos(), 0);
                context.client().player.swingHand(context.client().player.getActiveHand());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(WaxWorkstationPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().world.syncWorldEvent(context.client().player, WorldEvents.BLOCK_WAXED, payload.pos(), 0);
                context.client().player.swingHand(context.client().player.getActiveHand());
            });
        });
    }
}
