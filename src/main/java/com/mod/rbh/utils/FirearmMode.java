package com.mod.rbh.utils;

import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.items.SingularityBattery;
import com.mod.rbh.items.SingularityRifle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirearmMode {

    private final ItemStack ammoItem;

    // Aiming
    public final int aimTime;
    public final int unaimTime;

    public final int runningTime;

    @Nullable
    protected final SoundEvent aimSound;
    @Nullable protected final SoundEvent unaimSound;

    // Equip
    public final int equipTime;
    public final int unequipTime;

    private final RawAnimation equipAnim;
    private final RawAnimation unequipAnim;

    @Nullable
    protected final SoundEvent equipSound;
    @Nullable protected final SoundEvent unequipSound;

    public FirearmMode(int aimTime, int unaimTime, @Nullable SoundEvent aimSound, @Nullable SoundEvent unaimSound, int equipTime, int unequipTime, @Nullable SoundEvent equipSound, @Nullable SoundEvent unequipSound, RawAnimation equipAnim, RawAnimation unequipAnim, int runningTime, ItemStack ammo) {
        this.aimTime = aimTime;
        this.unaimTime = unaimTime;
        this.aimSound = aimSound;
        this.unaimSound = unaimSound;
        this.equipTime = equipTime;
        this.unequipTime = unequipTime;
        this.equipSound = equipSound;
        this.unequipSound = unequipSound;

        this.equipAnim = equipAnim;
        this.unequipAnim = unequipAnim;

        this.runningTime = runningTime;

        this.ammoItem = ammo;
    }

    public boolean canAim(ItemStack itemStack, LivingEntity entity) {
        SingularityRifle.Action action = FirearmDataUtils.getAction(itemStack);
        if (action != null && !action.canAim())
            return false;
        return !this.isRunning(itemStack, entity);
    }

    public void startAiming(ItemStack itemStack, LivingEntity entity) {
        if (!entity.level().isClientSide()) return;

        FirearmDataUtils.setAiming(itemStack, true);
        int currentUnaimingTime = this.getAimingTime(itemStack, entity);
        float frac = this.unaimTime == 0 ? 0 : (float) currentUnaimingTime / (float) this.unaimTime;
        frac = 1f - frac;
        this.setAimingTime(itemStack, entity, Mth.ceil(this.aimTime * frac));
        if (this.aimSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.aimSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void stopAiming(ItemStack itemStack, LivingEntity entity) {
        if (!entity.level().isClientSide()) return;

        FirearmDataUtils.setAiming(itemStack, false);
        int currentAimingTime = this.getAimingTime(itemStack, entity);
        float frac = this.aimTime == 0 ? 0 : (float) currentAimingTime / (float) this.aimTime;
        frac = 1f - frac;
        this.setAimingTime(itemStack, entity, Mth.ceil(this.unaimTime * frac));
        entity.stopUsingItem();
        if (this.unaimSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.unaimSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void equip(ItemStack itemStack, LivingEntity entity) {

        if (entity.level() instanceof ClientLevel) {
            ((SingularityRifle) itemStack.getItem()).stopTriggeredAnim(entity, GeoItem.getId(itemStack), "move", "equip");
            ((SingularityRifle) itemStack.getItem()).triggerAnim(entity, GeoItem.getId(itemStack), "move", "equip");
        }

        if (!entity.level().isClientSide()) return;

        int currentUnaimingTime = this.getEquipTime(itemStack, entity);
        float frac = this.unequipTime == 0 ? 0 : (float) currentUnaimingTime / (float) this.unequipTime;
        frac = 1f - frac;
        this.setEquipTime(itemStack, entity, Mth.ceil(this.equipTime * frac));

        if (this.equipSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.equipSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void unequip(ItemStack itemStack, LivingEntity entity) {
        if (!entity.level().isClientSide()) return;

        int currentAimingTime = this.getEquipTime(itemStack, entity);;
        float frac = this.equipTime == 0 ? 0 : (float) currentAimingTime / (float) this.equipTime;
        frac = 1f - frac;
        this.setEquipTime(itemStack, entity, Mth.ceil(this.unequipTime * frac));

        if (this.unequipSound != null)
            entity.level().playSound(entity, entity.blockPosition(), this.unequipSound, SoundSource.NEUTRAL, 1f, 1f);
    }

    public void startRunning(ItemStack itemStack, LivingEntity entity) {

        if (!entity.level().isClientSide()) return;

        FirearmDataUtils.setRunning(itemStack, true);
        int runTime = this.getRunTime(itemStack, entity);
        float frac = this.runningTime == 0 ? 0 : (float) runTime / (float) this.runningTime;
        frac = 1f - frac;
        this.setRTime(itemStack, entity, Mth.ceil(this.runningTime * frac));
    }

    public void stopRunning(ItemStack itemStack, LivingEntity entity) {

        if (!entity.level().isClientSide()) return;
        FirearmDataUtils.setRunning(itemStack, false);

        int runTime = this.getRunTime(itemStack, entity);;
        float frac = this.runningTime == 0 ? 0 : (float) runTime / (float) this.runningTime;
        frac = 1f - frac;
        this.setRTime(itemStack, entity, Mth.ceil(this.runningTime * frac));
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

    public int getRunTime(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.getRunTime(itemStack);
    }

    public void setRTime(ItemStack itemStack, LivingEntity entity, int time) {
        FirearmDataUtils.setRunTime(itemStack, time);
    }

    public int aimTime() { return this.aimTime; }

    public int unaimTime() { return this.unaimTime; }


    public int equipTime() { return this.equipTime; }

    public int unequipTime() { return this.unequipTime; }
    public int getRunningTime() { return this.runningTime; }

    public void tryRunningReloadAction(ItemStack itemStack, LivingEntity entity, ReloadPhaseType phaseType,
                                       boolean onInput, boolean firstReload) {
        if (entity instanceof ServerPlayer inventoryCarrier) {
            boolean hasBattery = inventoryCarrier.getInventory().hasAnyOf(new HashSet<>(List.of(new Item[]{ammoItem.getItem()})));

            if (!hasBattery) {
                inventoryCarrier.displayClientMessage(Component.literal("No Battery in your inventory"), true);
            }

            int bat1 = FirearmDataUtils.getBattery1Energy(itemStack);
            int bat2 = FirearmDataUtils.getBattery2Energy(itemStack);

            int slot = findMostChargedBatterySlot(inventoryCarrier);
            ItemStack itemInSlot = inventoryCarrier.getInventory().getItem(slot);

            if (!(itemInSlot.getItem() instanceof SingularityBattery)) {
                inventoryCarrier.displayClientMessage(Component.literal("SlotMismatchError"), true);
                return;
            }

            int engInBat = SingularityBattery.getEnergy(itemInSlot);

            if (bat1 >= bat2 && bat2 != SingularityBattery.MAX_ENERGY && engInBat > bat2) {
                // change bat 2
                FirearmDataUtils.setBattery2Energy(itemStack, engInBat);
                SingularityBattery.setEnergy(itemInSlot, bat2);
                inventoryCarrier.displayClientMessage(Component.literal("bat 2 refilled"), true);
            } else if (bat1 != SingularityBattery.MAX_ENERGY && engInBat > bat1) {
                // change bat 1
                FirearmDataUtils.setBattery1Energy(itemStack, engInBat);
                SingularityBattery.setEnergy(itemInSlot, bat1);
                inventoryCarrier.displayClientMessage(Component.literal("bat 1 refilled"), true);
            } else {
                inventoryCarrier.displayClientMessage(Component.literal("No need for reload"), true);
            }
        }

    }

    public static int findMostChargedBatterySlot(Player player) {
        int bestSlot = -1;
        int bestEnergy = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SingularityBattery) {
                int energy = SingularityBattery.getEnergy(stack);
                if (energy > bestEnergy) {
                    bestEnergy = energy;
                    bestSlot = i;
                }
            }
        }

        return bestSlot; // -1 if none found
    }

    public boolean isRunning(ItemStack itemStack, LivingEntity entity) {
        return entity.isSprinting();
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
        } else if (entity.level() instanceof ClientLevel) {
            ((SingularityRifle) itemStack.getItem()).triggerAnim(entity, GeoItem.getId(itemStack), "move", "idle");
        }

        int runningTime = this.getRunTime(itemStack, entity);
        if (runningTime > 0 && entity.level().isClientSide) {
            --runningTime;
            this.setRTime(itemStack, entity, runningTime);
        }

        if (FirearmDataUtils.isHoldingAttackKey(itemStack)) {
            int power = FirearmDataUtils.getChargeLevel(itemStack);
            if (power != 0) {
                float modifier = (float) FirearmDataUtils.getChargeLevel(itemStack) / SingularityRifle.MAX_CHARGE_LEVEL;
                Vec3 lookVector = entity.getLookAngle();
//                Vec3 additionalOffset = lookVector.multiply(0.5f, 0.5f, 0.5f);
                BlackHole hole = new BlackHole(entity.getEyePosition(), entity.level(), SingularityRifle.MAX_SIZE * modifier, SingularityRifle.MAX_EFFECT_SIZE * modifier, ((SingularityRifle) itemStack.getItem()).shouldBeColorful(itemStack));
                entity.level().addFreshEntity(hole);
                hole.shoot(lookVector.x, lookVector.y, lookVector.z, 2.2f, 0.01f);
                FirearmDataUtils.setChargeLevel(itemStack, 0);
            }
        }

        if (FirearmDataUtils.isCharging(itemStack) && !FirearmDataUtils.isHoldingAttackKey(itemStack)) {
            if (isSelected) {
                int nowChargeLevel = FirearmDataUtils.getChargeLevel(itemStack);

                if (nowChargeLevel < SingularityRifle.MAX_CHARGE_LEVEL) {
                    boolean bat1HasEnergy = FirearmDataUtils.getBattery1Energy(itemStack) > 0;
                    boolean bat2HasEnergy = FirearmDataUtils.getBattery2Energy(itemStack) > 0;
                    if (bat2HasEnergy) {
                        FirearmDataUtils.setBattery2Energy(itemStack, FirearmDataUtils.getBattery2Energy(itemStack) - 1);
                        FirearmDataUtils.setChargeLevel(itemStack, FirearmDataUtils.getChargeLevel(itemStack) + 1);

                    } else if (bat1HasEnergy) {
                        FirearmDataUtils.setBattery1Energy(itemStack, FirearmDataUtils.getBattery1Energy(itemStack) - 1);
                        FirearmDataUtils.setChargeLevel(itemStack, FirearmDataUtils.getChargeLevel(itemStack) + 1);
                    } else if (entity instanceof Player plr) {
                        plr.displayClientMessage(Component.literal("No energy!"), true);
                    }
                    if (entity instanceof Player plr && (bat1HasEnergy || bat2HasEnergy))
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

        if (entity.level().isClientSide) {
            boolean wasRunning = FirearmDataUtils.isRunning(itemStack);
            boolean isNowRunning = this.isRunning(itemStack, entity);

            if (isNowRunning && !wasRunning)
                this.startRunning(itemStack, entity);
            if (!isNowRunning && wasRunning)
                this.stopRunning(itemStack, entity);
        }

        FirearmDataUtils.setEquipped(itemStack, isSelected);
//        FirearmDataUtils.setRunning(itemStack, this.isRunning(itemStack, entity));
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
