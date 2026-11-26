package com.ratons.mixin;

import com.ratons.features.misc.rat.RatFreecam;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends the mouse into the development freecam instead of turning the player. */
@Mixin(Entity.class)
public abstract class EntityTurnMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void ratons$turnFreecam(double yRot, double xRot, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof LocalPlayer) || !RatFreecam.INSTANCE.getActive()) {
            return;
        }

        RatFreecam.INSTANCE.turn(yRot, xRot);
        ci.cancel();
    }
}
