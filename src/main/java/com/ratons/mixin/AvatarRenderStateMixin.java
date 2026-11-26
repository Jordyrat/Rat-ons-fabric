package com.ratons.mixin;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.ratons.features.misc.rat.RatRiderHolder;
import java.util.UUID;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements RatRiderHolder {

    @Unique
    private UUID ratons$rider;

    @Override
    public UUID ratons$getRider() {
        return this.ratons$rider;
    }

    @Override
    public void ratons$setRider(UUID uuid) {
        this.ratons$rider = uuid;
    }
}
