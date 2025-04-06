package net.diemond_player.waxed_workstations;

import eu.midnightdust.lib.config.MidnightConfig;
import net.diemond_player.waxed_workstations.payload.UnwaxWorkstationPacket;
import net.diemond_player.waxed_workstations.payload.WaxWorkstationPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Optional;

import static net.diemond_player.waxed_workstations.WaxedWorkstationsClient.UNWAX_WORKSTATION_PACKET_ID;
import static net.diemond_player.waxed_workstations.WaxedWorkstationsClient.WAX_WORKSTATION_PACKET_ID;

public class WaxedWorkstations implements ModInitializer {
    public static final String MOD_ID = "waxed_workstations";

	private static ActionResult interact(PlayerEntity playerEntity, World world, Hand hand) {
		ItemStack itemStack = playerEntity.getStackInHand(hand);
		if (!playerEntity.isSpectator()) {
			HitResult hitResult = playerEntity.raycast(4.5, 1.0F, false);
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult) hitResult;
				BlockPos blockPos = blockHitResult.getBlockPos();
				BlockState blockState = world.getBlockState(blockPos);
				if(blockState.getBlock() instanceof BedBlock && blockState.get(Properties.BED_PART) == BedPart.FOOT){
					blockPos = blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING));
					blockState = world.getBlockState(blockPos);
				}
				Optional<RegistryEntry<PointOfInterestType>> optional = PointOfInterestTypes.getTypeForState(blockState);
				if (optional.isPresent()) {
					if (optional.get().getKey().get() == PointOfInterestTypes.HOME) {
						if(!WaxedWorkstationsConfig.enableWaxingBeds) {
							return ActionResult.PASS;
						}
					} else if (optional.get().isIn(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE)) {
						if(!WaxedWorkstationsConfig.enableWaxingWorkstations) {
							return ActionResult.PASS;
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.MEETING) {
						if(!WaxedWorkstationsConfig.enableWaxingBells) {
							return ActionResult.PASS;
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.LODESTONE) {
						if(!WaxedWorkstationsConfig.enableWaxingLodestones) {
							return ActionResult.PASS;
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.LIGHTNING_ROD) {
						if(!WaxedWorkstationsConfig.enableWaxingLightningRods) {
							return ActionResult.PASS;
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.NETHER_PORTAL) {
						if(!WaxedWorkstationsConfig.enableWaxingNetherPortals) {
							return ActionResult.PASS;
						}
					} else if (optional.get().isIn(PointOfInterestTypeTags.BEE_HOME)) {
						if(!WaxedWorkstationsConfig.enableWaxingBeehives) {
							return ActionResult.PASS;
						}
					} else if (!WaxedWorkstationsConfig.enableWaxingEtc) {
						return ActionResult.PASS;
					}
				} else {
					return ActionResult.PASS;
				}
				if (WaxedWorkstationsConfig.waxingItems.contains(Registries.ITEM.getId(itemStack.getItem()).toString())
						|| WaxedWorkstationsConfig.waxingItems.stream().filter(s -> s.startsWith("tag ")).anyMatch(s -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(s.substring(4)))))) {
					if (!world.isClient()) {
						ServerWorld serverWorld = (ServerWorld) world;
						BlockPos blockPos2 = blockPos.toImmutable();
						if (serverWorld.getPointOfInterestStorage().hasTypeAt(optional.get().getKey().get(), blockPos2)) {
							serverWorld.getServer().execute(() -> {
								serverWorld.getPointOfInterestStorage().remove(blockPos2);
								DebugInfoSender.sendPoiRemoval(serverWorld, blockPos2);
							});

							ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new WaxWorkstationPacket(blockPos));
							Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
							world.syncWorldEvent(playerEntity, WorldEvents.BLOCK_WAXED, blockPos, 0);
							if(blockState.getBlock() instanceof BedBlock){
								ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new WaxWorkstationPacket(blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite())));
								world.syncWorldEvent(playerEntity, WorldEvents.BLOCK_WAXED, blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()), 0);
							}
							if (!playerEntity.isCreative()) {
								if (itemStack.isDamageable()) {
									itemStack.damage(WaxedWorkstationsConfig.waxingConsumeAmount, playerEntity, LivingEntity.getSlotForHand(hand));
								} else {
									itemStack.decrement(WaxedWorkstationsConfig.waxingConsumeAmount);
								}
							}
							return ActionResult.SUCCESS;
						}
					}

				} else if (WaxedWorkstationsConfig.unwaxingItems.contains(Registries.ITEM.getId(itemStack.getItem()).toString())
						|| WaxedWorkstationsConfig.unwaxingItems.stream().filter(s -> s.startsWith("tag ")).anyMatch(s -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(s.substring(4)))))) {
					if (!world.isClient()) {
						ServerWorld serverWorld = (ServerWorld) world;
						BlockPos blockPos2 = blockPos.toImmutable();
						if (!serverWorld.getPointOfInterestStorage().hasTypeAt(optional.get().getKey().get(), blockPos2)) {
							serverWorld.getServer().execute(() -> {
								serverWorld.getPointOfInterestStorage().add(blockPos2, optional.get());
								DebugInfoSender.sendPoiAddition(serverWorld, blockPos2);
							});

							ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new UnwaxWorkstationPacket(blockPos));
							world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
							world.syncWorldEvent(playerEntity, WorldEvents.WAX_REMOVED, blockPos, 0);
							Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
							if(blockState.getBlock() instanceof BedBlock){
								ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new UnwaxWorkstationPacket(blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite())));
								world.syncWorldEvent(playerEntity, WorldEvents.WAX_REMOVED, blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()), 0);
							}
							if (!playerEntity.isCreative()) {
								if (itemStack.isDamageable()) {
									itemStack.damage(WaxedWorkstationsConfig.unwaxingConsumeAmount, playerEntity, LivingEntity.getSlotForHand(hand));
								} else {
									itemStack.decrement(WaxedWorkstationsConfig.unwaxingConsumeAmount);
								}
							}
							return ActionResult.SUCCESS;
						}
					}

				}
			}
			return ActionResult.PASS;
		}
		return ActionResult.FAIL;
	}

	@Override
    public void onInitialize() {

		MidnightConfig.init(MOD_ID, WaxedWorkstationsConfig.class);

		PayloadTypeRegistry.playS2C().register(WaxWorkstationPacket.ID, WaxWorkstationPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(UnwaxWorkstationPacket.ID, UnwaxWorkstationPacket.CODEC);

		UseItemCallback.EVENT.register(WaxedWorkstations::interact);
	}
}