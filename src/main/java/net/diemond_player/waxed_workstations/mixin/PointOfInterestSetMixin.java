package net.diemond_player.waxed_workstations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PointOfInterestSet.class)
public class PointOfInterestSetMixin {
    @SuppressWarnings("mapping")
    @Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"), method = "remove", cancellable = true)
    private void waxed_workstations$remove(BlockPos pos, CallbackInfo ci, @Local PointOfInterest pointOfInterest) {
        if(pointOfInterest == null) {
            ci.cancel();
        }
    }
}