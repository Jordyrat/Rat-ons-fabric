package com.ratons.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {

    @Invoker("setPosition")
    void ratons$setPosition(Vec3 position);

    @Invoker("setRotation")
    void ratons$setRotation(float yaw, float pitch);

    @Invoker("createProjectionMatrixForCulling")
    Matrix4f ratons$createProjectionMatrixForCulling();

    @Invoker("prepareCullFrustum")
    void ratons$prepareCullFrustum(Matrix4fc projection, Matrix4f viewRotation, Vec3 position);
}
