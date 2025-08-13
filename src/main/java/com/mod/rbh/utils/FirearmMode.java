package com.mod.rbh.utils;

import com.mod.rbh.items.SingularityRifle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirearmMode {

    // Aiming
    protected final int aimTime;
    protected final int unaimTime;
    @Nullable
    protected final SoundEvent aimSound;
    @Nullable protected final SoundEvent unaimSound;

    public FirearmMode(int aimTime, int unaimTime, @Nullable SoundEvent aimSound, @Nullable SoundEvent unaimSound) {
        this.aimTime = aimTime;
        this.unaimTime = unaimTime;
        this.aimSound = aimSound;
        this.unaimSound = unaimSound;
    }
    
    public int getAimingTime(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.getAimingTime(itemStack);
    }

    public void setAimingTime(ItemStack itemStack, LivingEntity entity, int time) {
        FirearmDataUtils.setAimingTime(itemStack, time);
    }

    public void startAiming(ItemStack itemStack, LivingEntity entity) {
        FirearmDataUtils.setAiming(itemStack, true);
        int currentUnaimingTime = this.getAimingTime(itemStack, entity);
        float frac = this.unaimTime == 0 ? 0 : (float) currentUnaimingTime / (float) this.unaimTime;
        frac = 1f - frac;
        this.setAimingTime(itemStack, entity, Mth.ceil(this.aimTime * frac));
        if (this.aimSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.aimSound, SoundSource.NEUTRAL, 1f, 1f);
    }
    
    public boolean canAim(ItemStack itemStack, LivingEntity entity) {
        SingularityRifle.Action action = FirearmDataUtils.getAction(itemStack);
        return action == null || action.canAim();
    }
    
    public boolean isAiming(ItemStack itemStack, LivingEntity entity) {
        return entity instanceof Player ? entity.isUsingItem() : FirearmDataUtils.isAiming(itemStack);
    }

    public void stopAiming(ItemStack itemStack, LivingEntity entity) {
        FirearmDataUtils.setAiming(itemStack, false);
        int currentAimingTime = this.getAimingTime(itemStack, entity);;
        float frac = this.aimTime == 0 ? 0 : (float) currentAimingTime / (float) this.aimTime;
        frac = 1f - frac;
        this.setAimingTime(itemStack, entity, Mth.ceil(this.unaimTime * frac));
        entity.stopUsingItem();
        if (this.unaimSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.unaimSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void tryRunningReloadAction(ItemStack itemStack, LivingEntity entity, ReloadPhaseType phaseType,
                                       boolean onInput, boolean firstReload) {

    }

    public enum ReloadPhaseType implements StringRepresentable {
        PREPARE,
        RELOAD,
        FINISH;

        private static final Map<String, ReloadPhaseType> BY_ID = Arrays.stream(values())
                .collect(Collectors.toMap(ReloadPhaseType::getSerializedName, Function.identity()));

        private final String id = this.name().toLowerCase(Locale.ROOT);

        @Override public String getSerializedName() { return this.id; }

        @Nullable public static ReloadPhaseType byId(String id) { return BY_ID.get(id); }

        @Nullable
        public static ReloadPhaseType byIdUnload(String id) {
            if ("unload".equals(id))
                return RELOAD;
            if ("reload".equals(id))
                return null;
            return byId(id);
        }
    }
}
