package com.mod.rbh.mixin.client;

import com.mod.rbh.shaders.PostEffectRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(
            method = "initOutline",
            at = @At("TAIL")
    )
    private void reinforced_initOutline(CallbackInfo ci) {
        PostEffectRegistry.onInitializeOutline();
    }

    @Inject(
            method = "resize",
            at = @At("TAIL")
    )
    private void reinforced_resize(int width, int height, CallbackInfo ci) {
        PostEffectRegistry.resize(width, height);
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
                    shift = At.Shift.BEFORE
            )
    )
    private void reinforced_renderLevel_beforeEntities(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            Matrix4f modelViewMatrix,
            CallbackInfo ci
    ) {
        PostEffectRegistry.clearAndBindWrite(this.minecraft.getMainRenderTarget());
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void reinforced_renderLevel_process(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            Matrix4f modelViewMatrix,
            CallbackInfo ci
    ) {
        // If you need a mid-level pass later, put it here.
        // PostEffectRegistry.processEffects(
        //         this.minecraft.getMainRenderTarget(),
        //         deltaTracker.getGameTimeDeltaPartialTick(false),
        //         PostEffectRegistry.RenderPhase.AFTER_LEVEL
        // );
    }

    @Inject(
            method = "renderLevel",
            at = @At("TAIL")
    )
    private void reinforced_renderLevel_end(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            Matrix4f modelViewMatrix,
            CallbackInfo ci
    ) {
        PostEffectRegistry.processEffects(
                this.minecraft.getMainRenderTarget(),
                deltaTracker.getGameTimeDeltaPartialTick(false),
                PostEffectRegistry.RenderPhase.AFTER_LEVEL
        );
    }
}