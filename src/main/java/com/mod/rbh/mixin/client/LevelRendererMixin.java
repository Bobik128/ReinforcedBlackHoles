package com.mod.rbh.mixin.client;

import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class LevelRendererMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = {"Lnet/minecraft/client/renderer/LevelRenderer;initOutline()V"}, remap = true, at = {@At("TAIL")})
    private void reinforced_initOutline(CallbackInfo ci) {
        PostEffectRegistry.onInitializeOutline();
    }

    @Inject(method = {"Lnet/minecraft/client/renderer/LevelRenderer;resize(II)V"}, remap = true, at = {@At("TAIL")})
    private void reinforced_resize(int x, int y, CallbackInfo ci) {
        PostEffectRegistry.resize(x, y);
    }

    @Inject(method = {"Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"}, remap = true, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;", shift = At.Shift.BEFORE)})
    private void reinforced_renderLevel_beforeEntities(PoseStack poseStack, float f, long l, boolean b, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        PostEffectRegistry.clearAndBindWrite(this.minecraft.getMainRenderTarget());
    }

    @Inject(method = {"Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"}, remap = true, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.BEFORE)})
    private void reinforced_renderLevel_process(PoseStack poseStack, float f, long l, boolean b, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
//        PostEffectRegistry.processEffects(this.minecraft.getMainRenderTarget(), f);
    }

    @Inject(method = {"Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"}, remap = true, at = {@At("TAIL")})
    private void reinforced_renderLevel_end(PoseStack poseStack, float f, long l, boolean b, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
//        PostEffectRegistry.processEffects(Minecraft.getInstance().getMainRenderTarget(), minecraft.getPartialTick());
//        PostEffectRegistry.blitEffects();

        PostEffectRegistry.processEffects(Minecraft.getInstance().getMainRenderTarget(), minecraft.getPartialTick(), PostEffectRegistry.RenderPhase.AFTER_LEVEL);
    }
}
