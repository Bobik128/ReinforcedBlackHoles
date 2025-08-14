package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements IGameRenderer {
    @Unique
    private float reinforcedBlackHoles$cachedFov = 70.0f;

    @Inject(method = "getFov", at = @At("RETURN"))
    private void cacheFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting, CallbackInfoReturnable<Double> cir) {
        reinforcedBlackHoles$cachedFov = cir.getReturnValue().floatValue();
    }

    @Override
    public float getFovPublic() {
        return reinforcedBlackHoles$cachedFov;
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderLevel(float pPartialTicks, long pFinishTimeNano, PoseStack pMatrixStack, CallbackInfo ci) {
        PostEffectRegistry.processEffects(Minecraft.getInstance().getMainRenderTarget(), pPartialTicks);
        PostEffectRegistry.blitEffects();
    }
}
