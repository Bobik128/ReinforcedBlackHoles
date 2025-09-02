package com.mod.rbh.mixin.client;

import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.utils.FirearmMode;
import com.mod.rbh.utils.HandLagAdder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Unique
    private static final Vec3 reinforcedBlackHoles$equipedPos = new Vec3(0.3F, -0.46F, -0.7F);
    @Unique
    private static final Vec3 reinforcedBlackHoles$aimingPos = new Vec3(0f, -0.5F, -0.5F);

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack pPoseStack, HumanoidArm pHand, float pEquippedProg);

    @Shadow
    protected abstract void renderPlayerArm(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide);

    @Shadow @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderArmWithItem(AbstractClientPlayer player,
                                     float partialTicks,
                                     float pitch,
                                     InteractionHand hand,
                                     float swingProgress,
                                     ItemStack stack,
                                     float equippedProgress,
                                     PoseStack poseStack,
                                     MultiBufferSource buffer,
                                     int combinedLight,
                                     CallbackInfo ci) {

        if (stack.getItem() instanceof SingularityRifle rifle) {
            poseStack.pushPose();

            boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);

            if (!rightHand) {
                poseStack.popPose();
                ci.cancel();
                return;
            }

            HandLagAdder.lagHand(partialTicks, poseStack);

            float progress = reinforcedBlackHoles$getAimingProgress(player, stack, rifle.mode, partialTicks);
//            float equipProgress = reinforcedBlackHoles$getEquipProgress(player, stack, rifle, partialTicks);

            float k = 1;
            Vec3 finalPos = reinforcedBlackHoles$equipedPos.lerp(reinforcedBlackHoles$aimingPos, progress);

            poseStack.translate(k * finalPos.x, finalPos.y, finalPos.z);

            ((ItemInHandRenderer)(Object)this).renderItem(
                    player,
                    stack,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    false,
                    poseStack,
                    buffer,
                    combinedLight
            );

            poseStack.popPose();
            ci.cancel();
        }
    }

    @Unique
    private float reinforcedBlackHoles$getAimingProgress(AbstractClientPlayer player, ItemStack stack, FirearmMode mode, float partialTicks) {
        boolean isAiming = player.isUsingItem();

        int denom = mode.isAiming(stack, player) ? mode.aimTime() : mode.unaimTime();
        float aimingTime = (float) denom - mode.getAimingTime(stack, player);
        float frac = denom > 0 ? aimingTime / (float) denom : 1;
        float frac1 = denom > 0 ? partialTicks / (float) denom : 0;
        float d = isAiming ? frac + frac1 : 1 - frac - frac1;
        d = Mth.clamp(d, 0f, 1f);
        return d * d;
    }

    @Unique
    private float reinforcedBlackHoles$getEquipProgress(AbstractClientPlayer player, ItemStack stack, SingularityRifle rifle, float partialTicks) {
        boolean isEquiped = rifle.isEquiped(stack, player);
        FirearmMode mode = rifle.mode;

        int denom = isEquiped ? mode.equipTime() : mode.unequipTime();
        float equipTime = (float) denom - mode.getEquipTime(stack, player);
        float frac = denom > 0 ? equipTime / (float) denom : 1;
        float frac1 = denom > 0 ? partialTicks / (float) denom : 0;
        float d = isEquiped ? frac + frac1 : 1 - frac - frac1;
        d = Mth.clamp(d, 0f, 1f);
        return 1.0f - d * d;
    }

    @Inject(method = "renderHandsWithItems",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V",
                    shift = At.Shift.BEFORE))
    private void beforeEndBatch(float pPartialTicks, PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight, CallbackInfo ci) {
//        PostEffectRegistry.copyWholeArmDepth(Minecraft.getInstance().getMainRenderTarget());
        PostEffectRegistry.processEffects(Minecraft.getInstance().getMainRenderTarget(), pPartialTicks, PostEffectRegistry.RenderPhase.AFTER_ARM);
    }
}
