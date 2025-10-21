package com.mod.rbh.utils;

import com.mod.rbh.entity.BlackHoleProjectile;
import com.mod.rbh.items.SingularityBattery;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.network.packet.ClientboundShootPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.RawAnimation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirearmMode {

    private final ItemStack ammoItem;

    public final int reloadTime;

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

    public FirearmMode(int aimTime, int unaimTime, @Nullable SoundEvent aimSound, @Nullable SoundEvent unaimSound, int equipTime, int unequipTime, @Nullable SoundEvent equipSound, @Nullable SoundEvent unequipSound, RawAnimation equipAnim, RawAnimation unequipAnim, int runningTime, ItemStack ammo, int relTime) {
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

        this.reloadTime = relTime;

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

        if (entity.level().isClientSide) {
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

            switch (phaseType) {
                case PREPARE -> {
                    if (Objects.equals(FirearmDataUtils.getAction(itemStack), SingularityRifle.Action.RELOAD)) return;

                    boolean hasBattery = inventoryCarrier.getInventory().hasAnyOf(new HashSet<>(List.of(new Item[]{ammoItem.getItem()})));
                    if (!hasBattery) {
                        inventoryCarrier.displayClientMessage(Component.literal("No Battery in your inventory"), true);
                        return;
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

                    boolean reloading1 = false;
                    if (bat1 < bat2 || bat2 == SingularityBattery.MAX_ENERGY || engInBat <= bat2) {
                        if (bat1 != SingularityBattery.MAX_ENERGY && engInBat > bat1) {
                            // change bat 1
                            reloading1 = true;
                        } else {
                            inventoryCarrier.displayClientMessage(Component.literal("No need for reload"), true);
                            return;
                        }
                    }

                    FirearmDataUtils.setAction(itemStack, SingularityRifle.Action.RELOAD);
                    FirearmDataUtils.setActionTime(itemStack, reloadTime);

                    long id;
                    if (entity.level() instanceof ServerLevel sl) {
                        id = GeoItem.getOrAssignId(itemStack, sl);
                    } else id = GeoItem.getId(itemStack);

                    ((SingularityRifle) itemStack.getItem()).triggerAnim(entity, id, "reload", reloading1 ? "reload1" : "reload2");
                }
                case RELOAD -> {

                }
                case FINISH -> {
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
                        inventoryCarrier.displayClientMessage(Component.literal("Battery 2 refilled"), true);
                    } else if (bat1 != SingularityBattery.MAX_ENERGY && engInBat > bat1) {
                        // change bat 1
                        FirearmDataUtils.setBattery1Energy(itemStack, engInBat);
                        SingularityBattery.setEnergy(itemInSlot, bat1);
                        inventoryCarrier.displayClientMessage(Component.literal("Battery 1 refilled"), true);
                    } else {
                        inventoryCarrier.displayClientMessage(Component.literal("No need for reload"), true);
                    }

                    FirearmDataUtils.setAction(itemStack, null);

                    long id;
                    if (entity.level() instanceof ServerLevel sl) {
                        id = GeoItem.getOrAssignId(itemStack, sl);
                    } else id = GeoItem.getId(itemStack);
                    ((SingularityRifle) itemStack.getItem()).stopTriggeredAnim(entity, id, "reload", "reload1");
                    ((SingularityRifle) itemStack.getItem()).stopTriggeredAnim(entity, id, "reload", "reload2");
                }
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

    public static float getVolume(ItemStack stack) {
        return (float) FirearmDataUtils.getChargeLevel(stack) / SingularityRifle.MAX_CHARGE_LEVEL;
    }

    public void onTick(ItemStack itemStack, LivingEntity entity, boolean isSelected) {
        if (entity.level() instanceof ServerLevel sl)
            GeoItem.getOrAssignId(itemStack, sl);

        if (this.isAiming(itemStack, entity) && !this.canAim(itemStack, entity)) {
            this.stopAiming(itemStack, entity);
        }
        int aimingTime = this.getAimingTime(itemStack, entity);
        if (aimingTime > 0) {
            --aimingTime;
            this.setAimingTime(itemStack, entity, aimingTime);
        }

        long id = GeoItem.getId(itemStack);
        if (!isSelected && FirearmDataUtils.getAction(itemStack) == SingularityRifle.Action.RELOAD) {
            FirearmDataUtils.cancelReload(itemStack);
            if (entity.level() instanceof ServerLevel sl) {
                id = GeoItem.getOrAssignId(itemStack, sl);
            } else id = GeoItem.getId(itemStack);
            ((SingularityRifle) itemStack.getItem()).stopTriggeredAnim(entity, id, "reload", "reload1");
            ((SingularityRifle) itemStack.getItem()).stopTriggeredAnim(entity, id, "reload", "reload2");
        }

        int actionTime = FirearmDataUtils.getActionTime(itemStack);
        if (actionTime > 0) {
            --actionTime;
            if (FirearmDataUtils.getAction(itemStack) == SingularityRifle.Action.RELOAD) {
                tryRunningReloadAction(itemStack, entity, actionTime > 0 ? ReloadPhaseType.RELOAD : ReloadPhaseType.FINISH, false, false);

            }
            entity.setSprinting(false);
            FirearmDataUtils.setActionTime(itemStack, actionTime);
        }

        int equipTime = this.getEquipTime(itemStack, entity);

        if (equipTime > 0) {
            --equipTime;
            this.setEquipTime(itemStack, entity, equipTime);
        } else if (entity.level().isClientSide && FirearmDataUtils.getAction(itemStack) == null) {
            ((SingularityRifle) itemStack.getItem()).triggerAnim(entity, GeoItem.getId(itemStack), "move", "idle");
        }

        if (FirearmDataUtils.isHoldingAttackKey(itemStack) && !entity.level().isClientSide) {
            int power = FirearmDataUtils.getChargeLevel(itemStack);
            if (power != 0) {
                float modifier = (float) FirearmDataUtils.getChargeLevel(itemStack) / SingularityRifle.MAX_CHARGE_LEVEL;
                Vec3 lookVector = entity.getLookAngle();
//                Vec3 additionalOffset = lookVector.multiply(0.5f, 0.5f, 0.5f);
                BlackHoleProjectile hole = new BlackHoleProjectile(entity.getEyePosition().add(entity.getLookAngle().scale(0.5f)), entity.level(), SingularityRifle.MAX_SIZE * modifier, SingularityRifle.MAX_EFFECT_SIZE * modifier, ((SingularityRifle) itemStack.getItem()).shouldBeColorful(itemStack));
                entity.level().addFreshEntity(hole);
                hole.shoot(lookVector.x, lookVector.y, lookVector.z, 6.0f, 0.01f);
                if (entity.level() instanceof ServerLevel) RBHNetwork.sendToAllInDimension(new ClientboundShootPacket(GeoItem.getId(itemStack), FirearmDataUtils.getChargeLevel(itemStack)), entity.level());
                FirearmDataUtils.setChargeLevel(itemStack, 0);
            }
        }

        if (FirearmDataUtils.isCharging(itemStack) && !FirearmDataUtils.isHoldingAttackKey(itemStack) && !entity.level().isClientSide) {
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
//                    if (entity instanceof Player plr && (bat1HasEnergy || bat2HasEnergy))
//                        plr.displayClientMessage(Component.literal("Rifle charge level is: " + FirearmDataUtils.getChargeLevel(itemStack)), true);
                } else {
                    if (entity instanceof Player plr)
                        plr.displayClientMessage(Component.literal("Rifle charge level is at max"), true);
                    FirearmDataUtils.setCharging(itemStack, false);
                }

            } else {
                FirearmDataUtils.setCharging(itemStack, false);
            }
        }

        if (entity.level().isClientSide) {
            int finalActionTime = actionTime;
            long finalId = id;
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
                    FirearmModeClient.clientTick(this, itemStack, entity, isSelected, finalId, finalActionTime)
            );
        }


        if (isSelected && !FirearmDataUtils.isEquipped(itemStack)) equip(itemStack, entity);
        if (!isSelected && FirearmDataUtils.isEquipped(itemStack)) unequip(itemStack, entity);

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
