package com.ratons.mixin;

import com.ratons.features.misc.rat.RatShoulderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.model.player.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives every avatar renderer the rat layer, the same way vanilla adds its shoulder parrot. */
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void ratons$addRatLayer(CallbackInfo ci) {
        RenderLayerParent<AvatarRenderState, PlayerModel> parent = (RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) this;
        ((LivingEntityRendererInvoker) (Object) this).ratons$addLayer(new RatShoulderLayer(parent));
    }
}
