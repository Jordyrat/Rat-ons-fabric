package com.ratons.mixin;

import com.ratons.features.misc.rat.RatRiderHolder;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stamps the rider's UUID onto the render state the rat layer will be handed. */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void ratons$rememberRider(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (cir.getReturnValue() instanceof RatRiderHolder holder) {
            holder.ratons$setRider(entity.getUUID());
        }
    }
}
