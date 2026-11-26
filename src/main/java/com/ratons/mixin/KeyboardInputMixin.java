package com.ratons.mixin;

import com.ratons.features.misc.rat.RatFreecam;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the player standing still while the development freecam moves the camera around. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void ratons$freezeWhileFreecam(CallbackInfo ci) {
        if (!RatFreecam.INSTANCE.getActive()) {
            return;
        }

        KeyboardInput input = (KeyboardInput) (Object) this;
        ((ClientInputAccessor) input).ratons$setMoveVector(Vec2.ZERO);
    }
}
