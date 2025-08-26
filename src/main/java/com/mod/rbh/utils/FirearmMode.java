package com.mod.rbh.utils;

import com.mod.rbh.items.SingularityRifle;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirearmMode {

    // Aiming
    public final int aimTime;
    public final int unaimTime;
    @Nullable
    protected final SoundEvent aimSound;
    @Nullable protected final SoundEvent unaimSound;

    // Equip
    public final int equipTime;
    public final int unequipTime;
    @Nullable
    protected final SoundEvent equipSound;
    @Nullable protected final SoundEvent unequipSound;

    public FirearmMode(int aimTime, int unaimTime, @Nullable SoundEvent aimSound, @Nullable SoundEvent unaimSound, int equipTime, int unequipTime, @Nullable SoundEvent equipSound, @Nullable SoundEvent unequipSound) {
        this.aimTime = aimTime;
        this.unaimTime = unaimTime;
        this.aimSound = aimSound;
        this.unaimSound = unaimSound;
        this.equipTime = equipTime;
        this.unequipTime = unequipTime;
        this.equipSound = equipSound;
        this.unequipSound = unequipSound;
    }

    public boolean canAim(ItemStack itemStack, LivingEntity entity) {
        SingularityRifle.Action action = FirearmDataUtils.getAction(itemStack);
        if (action != null && !action.canAim())
            return false;
        return true;
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

    public void equip(ItemStack itemStack, LivingEntity entity) {
        int currentUnaimingTime = this.getEquipTime(itemStack, entity);
        float frac = this.unequipTime == 0 ? 0 : (float) currentUnaimingTime / (float) this.unequipTime;
        frac = 1f - frac;
        this.setEquipTime(itemStack, entity, Mth.ceil(this.equipTime * frac));
        if (this.equipSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.equipSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void unequip(ItemStack itemStack, LivingEntity entity) {
        int currentAimingTime = this.getEquipTime(itemStack, entity);;
        float frac = this.equipTime == 0 ? 0 : (float) currentAimingTime / (float) this.equipTime;
        frac = 1f - frac;
        this.setEquipTime(itemStack, entity, Mth.ceil(this.unequipTime * frac));
        if (this.unequipSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.unequipSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public boolean isAiming(ItemStack itemStack, LivingEntity entity) {
        return entity instanceof Player ? entity.isUsingItem() : FirearmDataUtils.isAiming(itemStack);
    }

    public int getAimingTime(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.getAimingTime(itemStack);
    }

    public void setAimingTime(ItemStack itemStack, LivingEntity entity, int time) {
        FirearmDataUtils.setAimingTime(itemStack, time);
    }

    public int getEquipTime(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.getEQTime(itemStack);
    }

    public void setEquipTime(ItemStack itemStack, LivingEntity entity, int time) {
        FirearmDataUtils.setEQTime(itemStack, time);
    }

    public int aimTime() { return this.aimTime; }

    public int unaimTime() { return this.unaimTime; }


    public int equipTime() { return this.equipTime; }

    public int unequipTime() { return this.unequipTime; }

    public void tryRunningReloadAction(ItemStack itemStack, LivingEntity entity, ReloadPhaseType phaseType,
                                       boolean onInput, boolean firstReload) {

    }

    public void onTick(ItemStack itemStack, LivingEntity entity, boolean isSelected) {
        if (this.isAiming(itemStack, entity) && !this.canAim(itemStack, entity)) {
            this.stopAiming(itemStack, entity);
        }
        int aimingTime = this.getAimingTime(itemStack, entity);
        if (aimingTime > 0) {
            --aimingTime;
            this.setAimingTime(itemStack, entity, aimingTime);
        }
        int equipTime = this.getEquipTime(itemStack, entity);
        if (equipTime > 0) {
            --equipTime;
            this.setEquipTime(itemStack, entity, equipTime);
        }

        if (FirearmDataUtils.isCharging(itemStack)) {
            if (isSelected) {
                int nowChargeLevel = FirearmDataUtils.getChargeLevel(itemStack);

                if (nowChargeLevel < SingularityRifle.MAX_CHARGE_LEVEL) {
                    FirearmDataUtils.setChargeLevel(itemStack, FirearmDataUtils.getChargeLevel(itemStack) + 1);
                    if (entity instanceof Player plr)
                        plr.displayClientMessage(Component.literal("Rifle charge level is: " + FirearmDataUtils.getChargeLevel(itemStack)), true);
                } else {
                    if (entity instanceof Player plr)
                        plr.displayClientMessage(Component.literal("Rifle charge level is at max"), true);
                    FirearmDataUtils.setCharging(itemStack, false);
                }

            } else {
                FirearmDataUtils.setCharging(itemStack, false);
            }
        }

        if (isSelected && !FirearmDataUtils.isEquipped(itemStack)) equip(itemStack, entity);
        if (!isSelected && FirearmDataUtils.isEquipped(itemStack)) unequip(itemStack, entity);

        FirearmDataUtils.setEquipped(itemStack, isSelected);
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
