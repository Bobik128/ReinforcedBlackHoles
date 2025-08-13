package com.mod.rbh.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface HoldAttackKeyInteraction {

    // all are processed on both client and server
    boolean isHoldingAttackKey(ItemStack itemStack, LivingEntity entity);
    boolean onPressAttackKey(ItemStack itemStack, LivingEntity entity);
    void onReleaseAttackKey(ItemStack itemStack, LivingEntity entity);

}
