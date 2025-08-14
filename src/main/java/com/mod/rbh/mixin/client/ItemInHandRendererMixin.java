package com.mod.rbh.mixin.client;

import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.utils.FirearmMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
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

    private final Vec3 unequipedPos = new Vec3(0.56F, -1.2F, -0.1F);
    private final Vec3 equipedPos = new Vec3(0.45F, -0.52F, -0.9F);
    private final Vec3 aimingPos = new Vec3(0f, -0.5F, -0.5F);

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
            boolean isAiming = rifle.isAiming(stack, player);
            poseStack.pushPose();

            boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);

            float progress = reinforcedBlackHoles$getAimingProgress(player, stack, rifle.mode, partialTicks);
            float equipProgress = reinforcedBlackHoles$getEquipProgress(player, stack, rifle, partialTicks);

            float k = rightHand ? 1 : -1;
            float xRot = 0f;
            Vec3 finalPos = equipedPos.lerp(aimingPos, progress);
            finalPos = finalPos.lerp(unequipedPos, equipProgress);
            xRot = Mth.lerp(equipProgress, 0f, 80f);

            poseStack.translate(k * finalPos.x, finalPos.y, finalPos.z);
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

            ((ItemInHandRenderer)(Object)this).renderItem(
                    player,
                    stack,
                    rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                    !rightHand,
                    poseStack,
                    buffer,
                    combinedLight
            );

            poseStack.translate(0.4f * k, 0.1f, 0.95f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

            PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(player);
            if (rightHand) {
                playerrenderer.renderRightHand(poseStack, buffer, combinedLight, player);
            } else {
                playerrenderer.renderLeftHand(poseStack, buffer, combinedLight, player);
            }

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
}
