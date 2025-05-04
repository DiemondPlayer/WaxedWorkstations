package net.diemond_player.waxed_workstations;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
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
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Optional;

public class WaxedWorkstations implements ModInitializer {
    public static final String MOD_ID = "waxed_workstations";
	public static final String MIDNIGHT_LIB_MOD_ID = "midnightlib";
    public static final Identifier WAX_WORKSTATION_PACKET_ID = new Identifier(WaxedWorkstations.MOD_ID, "wax_workstation");
    public static final Identifier UNWAX_WORKSTATION_PACKET_ID = new Identifier(WaxedWorkstations.MOD_ID, "unwax_workstation");

	private static TypedActionResult<ItemStack> interact(PlayerEntity playerEntity, World world, Hand hand) {
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
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().isIn(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE)) {
						if(!WaxedWorkstationsConfig.enableWaxingWorkstations) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.MEETING) {
						if(!WaxedWorkstationsConfig.enableWaxingBells) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.LODESTONE) {
						if(!WaxedWorkstationsConfig.enableWaxingLodestones) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.LIGHTNING_ROD) {
						if(!WaxedWorkstationsConfig.enableWaxingLightningRods) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().getKey().get() == PointOfInterestTypes.NETHER_PORTAL) {
						if(!WaxedWorkstationsConfig.enableWaxingNetherPortals) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (optional.get().isIn(PointOfInterestTypeTags.BEE_HOME)) {
						if(!WaxedWorkstationsConfig.enableWaxingBeehives) {
							return TypedActionResult.pass(itemStack);
						}
					} else if (!WaxedWorkstationsConfig.enableWaxingEtc) {
						return TypedActionResult.pass(itemStack);
					}
				} else {
					return TypedActionResult.pass(itemStack);
				}
				if (WaxedWorkstationsConfig.waxingItems.contains(Registries.ITEM.getId(itemStack.getItem()).toString())
						|| WaxedWorkstationsConfig.waxingItems.stream().filter(s -> s.startsWith("tag ")).anyMatch(s -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, new Identifier(s.substring(4)))))) {
					if (!world.isClient()) {
						ServerWorld serverWorld = (ServerWorld) world;
						BlockPos blockPos2 = blockPos.toImmutable();
						if (serverWorld.getPointOfInterestStorage().hasTypeAt(optional.get().getKey().get(), blockPos2)) {
							serverWorld.getServer().execute(() -> {
								serverWorld.getPointOfInterestStorage().remove(blockPos2);
								DebugInfoSender.sendPoiRemoval(serverWorld, blockPos2);
							});
							PacketByteBuf buf = PacketByteBufs.create();
							buf.writeBlockPos(blockPos);

							ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, WAX_WORKSTATION_PACKET_ID, buf);
							Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
							world.syncWorldEvent(playerEntity, WorldEvents.BLOCK_WAXED, blockPos, 0);
							if(blockState.getBlock() instanceof BedBlock){
								PacketByteBuf buf1 = PacketByteBufs.create();
								buf1.writeBlockPos(blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()));
								ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, WAX_WORKSTATION_PACKET_ID, buf1);
								world.syncWorldEvent(playerEntity, WorldEvents.BLOCK_WAXED, blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()), 0);
							}
							if (!playerEntity.isCreative()) {
								if (itemStack.isDamageable()) {
									itemStack.damage(WaxedWorkstationsConfig.waxingConsumeAmount, playerEntity, p -> p.sendToolBreakStatus(hand));
								} else {
									itemStack.decrement(WaxedWorkstationsConfig.waxingConsumeAmount);
								}
							}
							return TypedActionResult.success(itemStack, true);
						}
					}
				} else if (WaxedWorkstationsConfig.unwaxingItems.contains(Registries.ITEM.getId(itemStack.getItem()).toString())
						|| WaxedWorkstationsConfig.unwaxingItems.stream().filter(s -> s.startsWith("tag ")).anyMatch(s -> itemStack.isIn(TagKey.of(RegistryKeys.ITEM, new Identifier(s.substring(4)))))) {
					if (!world.isClient()) {
						ServerWorld serverWorld = (ServerWorld) world;
						BlockPos blockPos2 = blockPos.toImmutable();
						if (!serverWorld.getPointOfInterestStorage().hasTypeAt(optional.get().getKey().get(), blockPos2)) {
							serverWorld.getServer().execute(() -> {
								serverWorld.getPointOfInterestStorage().add(blockPos2, optional.get());
								DebugInfoSender.sendPoiAddition(serverWorld, blockPos2);
							});
							PacketByteBuf buf = PacketByteBufs.create();
							buf.writeBlockPos(blockPos);

							ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, UNWAX_WORKSTATION_PACKET_ID, buf);
							world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
							world.syncWorldEvent(playerEntity, WorldEvents.WAX_REMOVED, blockPos, 0);
							Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
							if(blockState.getBlock() instanceof BedBlock){
								PacketByteBuf buf1 = PacketByteBufs.create();
								buf1.writeBlockPos(blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()));
								ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, UNWAX_WORKSTATION_PACKET_ID, buf1);
								world.syncWorldEvent(playerEntity, WorldEvents.WAX_REMOVED, blockPos.offset(blockState.get(Properties.HORIZONTAL_FACING).getOpposite()), 0);
							}
							if (!playerEntity.isCreative()) {
								if (itemStack.isDamageable()) {
									itemStack.damage(WaxedWorkstationsConfig.unwaxingConsumeAmount, playerEntity, p -> p.sendToolBreakStatus(hand));
								} else {
									itemStack.decrement(WaxedWorkstationsConfig.unwaxingConsumeAmount);
								}
							}
							return TypedActionResult.success(itemStack, true);
						}
					}
				}
			}
			return TypedActionResult.pass(itemStack);
		}
		return TypedActionResult.fail(itemStack);
	}

	@Override
    public void onInitialize() {
		if(FabricLoader.getInstance().isModLoaded(MIDNIGHT_LIB_MOD_ID)) {
			MidnightConfig.init(MOD_ID, WaxedWorkstationsConfig.class);
		}

		UseItemCallback.EVENT.register(WaxedWorkstations::interact);
	}
}