package com.mod.rbh.mixin.client;

import com.mod.rbh.items.RBHItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void getArmPose(AbstractClientPlayer pPlayer, InteractionHand pHand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.is(RBHItems.SINGULARITY_RIFLE.get())) {
            cir.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
        }
    }
}
