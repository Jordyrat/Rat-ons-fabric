package com.ratons.mixin;

import com.ratons.features.misc.rat.RatFreecam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Moves the camera off the player while the development freecam is on. */
@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccessor {

    @Inject(method = "update", at = @At("TAIL"))
    private void ratons$freecam(DeltaTracker tracker, CallbackInfo ci) {
        if (!RatFreecam.INSTANCE.getActive()) {
            return;
        }

        Vec3 position = RatFreecam.INSTANCE.position();
        if (position == null) {
            return;
        }

        CameraAccessor accessor = (CameraAccessor) this;
        accessor.ratons$setPosition(position);
        accessor.ratons$setRotation(RatFreecam.INSTANCE.yaw(), RatFreecam.INSTANCE.pitch());

        Camera camera = (Camera) (Object) this;
        accessor.ratons$prepareCullFrustum(
                accessor.ratons$createProjectionMatrixForCulling(),
                camera.getViewRotationMatrix(new Matrix4f()),
                position);
    }
}
