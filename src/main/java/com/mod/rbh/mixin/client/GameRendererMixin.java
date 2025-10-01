package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.compat.ShaderCompat;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements IGameRenderer {
    @Shadow
    private float fov;
    @Shadow
    private float oldFov;
    @Shadow
    private boolean panoramicMode;

    @Unique
    private float reinforcedBlackHoles$cachedFov = 70.0f;

    @Inject(method = "getFov", at = @At("RETURN"))
    private void cacheFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting, CallbackInfoReturnable<Double> cir) {
        reinforcedBlackHoles$cachedFov = cir.getReturnValue().floatValue();
    }

    @Override
    public float getFovPublic() {
        if (ShaderCompat.shadersEnabled()) {
            GameRenderer renderer = (GameRenderer) (Object) this;
            Minecraft minecraft = Minecraft.getInstance();
            Camera pActiveRenderInfo = renderer.getMainCamera();

            if (panoramicMode) {
                return 90.0f;
            } else {
                double d0 = 70.0D;
                if (true) {
                    d0 = (float) minecraft.options.fov().get().intValue();
                    d0 *= Mth.lerp(minecraft.getPartialTick(), oldFov, fov);
                }

                if (pActiveRenderInfo.getEntity() instanceof LivingEntity && ((LivingEntity) pActiveRenderInfo.getEntity()).isDeadOrDying()) {
                    float f = Math.min((float) ((LivingEntity) pActiveRenderInfo.getEntity()).deathTime + minecraft.getPartialTick(), 20.0F);
                    d0 /= (1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F;
                }

                FogType fogtype = pActiveRenderInfo.getFluidInCamera();
                if (fogtype == FogType.LAVA || fogtype == FogType.WATER) {
                    d0 *= Mth.lerp(minecraft.options.fovEffectScale().get(), 1.0D, (double) 0.85714287F);
                }

                return (float) net.minecraftforge.client.ForgeHooksClient.getFieldOfView(renderer, pActiveRenderInfo, minecraft.getPartialTick(), d0, true);
            }
        }
        return reinforcedBlackHoles$cachedFov;
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderLevel(float pPartialTicks, long pFinishTimeNano, PoseStack pMatrixStack, CallbackInfo ci) {
        PostEffectRegistry.blitEffects();
    }
}
