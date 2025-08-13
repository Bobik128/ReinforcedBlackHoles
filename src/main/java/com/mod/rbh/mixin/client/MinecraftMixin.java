package com.mod.rbh.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mod.rbh.remix.RBHClientRemix;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Shadow @Final
    public Options options;

    @Shadow @Final public MouseHandler mouseHandler;

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 10))
    private boolean rbh$handleKeybinds$useAttack(KeyMapping instance, Operation<Boolean> original) {
        boolean result = original.call(instance);
        if (result)
            RBHClientRemix.handleAttackKeybinds();
        return result;
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 13))
    private boolean rbh$handleKeybinds$attack(KeyMapping instance, Operation<Boolean> original) {
        boolean result = original.call(instance);
        if (result)
            RBHClientRemix.handleAttackKeybinds();
        return result;
    }

    @WrapMethod(method = "continueAttack")
    private void rbh$continueAttack(boolean leftClick, Operation<Void> original) {
        original.call(leftClick);
        boolean dontInterruptFiring = this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed();
        if (!dontInterruptFiring)
            RBHClientRemix.interruptAttack();
    }

}