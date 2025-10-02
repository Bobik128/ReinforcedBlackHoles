package com.mod.rbh.utils;

import com.mod.rbh.items.SingularityRifle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class FirearmDataUtils {

    public static void setActionTime(ItemStack itemStack, int cooldown) {
        itemStack.getOrCreateTag().putInt("ActionTime", cooldown);
    }

    public static int getActionTime(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("ActionTime");
    }

    public static void setAction(ItemStack itemStack, @Nullable SingularityRifle.Action action) {
        if (action != null) {
            itemStack.getOrCreateTag().putString("Action", action.getSerializedName());
        } else {
            itemStack.getOrCreateTag().remove("Action");
        }
    }

    public static void cancelReload(ItemStack itemStack) {
        FirearmDataUtils.setAction(itemStack, null);
        FirearmDataUtils.setActionTime(itemStack, 0);
    }

    @Nullable
    public static SingularityRifle.Action getAction(ItemStack itemStack) {
        return SingularityRifle.Action.byId(itemStack.getOrCreateTag().getString("Action"));
    }

    // Heating methods

    public static void setHeat(ItemStack itemStack, float heat) {
        setHeat(itemStack.getOrCreateTag(), heat);
    }

    public static void setHeat(CompoundTag tag, float heat) {
        tag.putFloat("FirearmHeat", heat);
    }

    public static float getHeat(ItemStack itemStack) {
        return getHeat(itemStack.getOrCreateTag());
    }

    public static float getHeat(CompoundTag tag) {
        return tag.getFloat("FirearmHeat");
    }

    public static void addHeat(ItemStack itemStack, float addedHeat) {
        addHeat(itemStack.getOrCreateTag(), addedHeat);
    }

    public static void addHeat(CompoundTag tag, float addedHeat) {
        setHeat(tag, getHeat(tag) + addedHeat);
    }

    public static void setCoolingDelay(ItemStack itemStack, int delay) {
        setCoolingDelay(itemStack.getOrCreateTag(), delay);
    }

    public static void setCoolingDelay(CompoundTag tag, int delay) {
        tag.putInt("CoolingDelay", delay);
    }

    public static int getCoolingDelay(ItemStack itemStack) {
        return getCoolingDelay(itemStack.getOrCreateTag());
    }

    public static int getCoolingDelay(CompoundTag tag) {
        return tag.getInt("CoolingDelay");
    }

    public static void setOverheated(ItemStack itemStack, boolean overheated) {
        setOverheated(itemStack.getOrCreateTag(), overheated);
    }

    public static void setOverheated(CompoundTag tag, boolean overheated) {
        if (overheated) {
            tag.putBoolean("Overheated", true);
        } else {
            tag.remove("Overheated");
        }
    }

    public static boolean isOverheated(ItemStack itemStack) {
        return isOverheated(itemStack.getOrCreateTag());
    }

    public static boolean isOverheated(CompoundTag tag) {
        return tag.contains("Overheated");
    }

    // Key methods

    public static void setHoldingAttackKey(ItemStack itemStack, boolean holdingAttackKey) {
        if (holdingAttackKey) {
            itemStack.getOrCreateTag().putBoolean("HoldingAttackKey", true);
        } else {
            itemStack.getOrCreateTag().remove("HoldingAttackKey");
        }
    }

    public static boolean isHoldingAttackKey(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("HoldingAttackKey");
    }

    // Equipped methods

    public static void setEquipped(ItemStack itemStack, boolean holdingAttackKey) {
        if (holdingAttackKey) {
            itemStack.getOrCreateTag().putBoolean("equippedLastTick", true);
        } else {
            itemStack.getOrCreateTag().remove("equippedLastTick");
        }
    }

    public static boolean isEquipped(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("equippedLastTick");
    }

    public static void setRunning(ItemStack itemStack, boolean holdingAttackKey) {
        if (holdingAttackKey) {
            itemStack.getOrCreateTag().putBoolean("runningLastTick", true);
        } else {
            itemStack.getOrCreateTag().remove("runningLastTick");
        }
    }

    public static boolean isRunning(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("runningLastTick");
    }

    // Equipped methods

    public static void setCharging(ItemStack itemStack, boolean holdingAttackKey) {
        if (holdingAttackKey) {
            itemStack.getOrCreateTag().putBoolean("chargingRifle", true);
        } else {
            itemStack.getOrCreateTag().remove("chargingRifle");
        }
    }

    public static boolean isCharging(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("chargingRifle");
    }

    // Aiming methods

    public static void setAiming(ItemStack itemStack, boolean aiming) {
        if (aiming) {
            itemStack.getOrCreateTag().putBoolean("Aiming", aiming);
        } else {
            itemStack.getOrCreateTag().remove("Aiming");
        }
    }

    public static boolean isAiming(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("Aiming");
    }

    public static void setAimingTime(ItemStack itemStack, int time) {
        itemStack.getOrCreateTag().putInt("AimingTime", time);
    }

    public static int getAimingTime(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("AimingTime");
    }

    // battery 1
    public static void setBattery1Energy(ItemStack item, int energy) {
        item.getOrCreateTag().putInt("Battery1Energy", energy);
    }

    public static int getBattery1Energy(ItemStack item) {
        return item.getOrCreateTag().getInt("Battery1Energy");
    }

    // battery 2
    public static void setBattery2Energy(ItemStack item, int energy) {
        item.getOrCreateTag().putInt("Battery2Energy", energy);
    }

    public static int getBattery2Energy(ItemStack item) {
        return item.getOrCreateTag().getInt("Battery2Energy");
    }

    public static void setEQTime(ItemStack itemStack, int time) {
        itemStack.getOrCreateTag().putInt("EQTime", time);
    }

    public static int getEQTime(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("EQTime");
    }

    public static void setRunTime(ItemStack itemStack, int time) {
        itemStack.getOrCreateTag().putInt("RunTime", time);
    }

    public static int getRunTime(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("RunTime");
    }

    public static void setChargeLevel(ItemStack itemStack, int level) {
        itemStack.getOrCreateTag().putInt("ChargeLevel", level);
    }

    public static int getChargeLevel(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt("ChargeLevel");
    }

    public static void setColor(ItemStack itemStack, int time) {
        itemStack.getOrCreateTag().putInt("Color", time);
    }

    public static int getColor(ItemStack itemStack) {
        if (!itemStack.getOrCreateTag().contains("Color")) {
            return SingularityRifle.BASE_COLOR;
        }
        return itemStack.getOrCreateTag().getInt("Color");
    }


    private FirearmDataUtils() {
    }

}
