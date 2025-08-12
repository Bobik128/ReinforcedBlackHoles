package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IShaderInstance;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin implements IShaderInstance {

    @Shadow
    @Nullable
    public abstract Uniform getUniform(String pName);

    @Unique
    @Nullable
    private Uniform BLACK_HOLE_CENTER;

    @Inject(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", at = @At("TAIL"))
    private void init(ResourceProvider pResourceProvider, ResourceLocation shaderLocation, VertexFormat pVertexFormat, CallbackInfo ci) {
        BLACK_HOLE_CENTER = getUniform("BlackHoleCenter");
    }

    @Unique
    @Override
    public void setBlackHoleCenter(Vec3 center) {
        if (BLACK_HOLE_CENTER == null) return;
        BLACK_HOLE_CENTER.set((float) center.x, (float) center.y, (float) center.z);
    }
}
