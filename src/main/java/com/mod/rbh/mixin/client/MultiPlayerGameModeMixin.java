package com.mod.rbh.mixin.client;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mod.rbh.items.SingularityRifle;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @WrapMethod(method = "attack")
    private void rbh$attack(Player pPlayer, Entity pTargetEntity, Operation<Void> original) {
        ItemStack mainhand = pPlayer.getMainHandItem();
        if (mainhand.getItem() instanceof SingularityRifle)
            return;

        original.call(pPlayer, pTargetEntity);
    }

}
