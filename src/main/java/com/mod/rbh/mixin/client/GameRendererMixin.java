package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IGameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
