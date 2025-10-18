package com.mod.rbh.items;

import com.mod.rbh.RBHClient;
import com.mod.rbh.api.FovModifyingItem;
import com.mod.rbh.api.HoldAttackKeyInteraction;
import com.mod.rbh.entity.ItemEntity.SingularityRifleItemEntity;
import com.mod.rbh.items.renderer.SingularityRifleRenderer;
import com.mod.rbh.sound.RBHSounds;
import com.mod.rbh.utils.FirearmDataUtils;
import com.mod.rbh.utils.FirearmMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingularityRifle extends Item implements GeoItem, FovModifyingItem, HoldAttackKeyInteraction {
    private static final RawAnimation IDLE_ANIM_SPIN = RawAnimation.begin().thenPlay("animation.rifle.idle_spin");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("animation.rifle.idle");
    private static final RawAnimation EQUIP_ANIM = RawAnimation.begin().thenPlay("animation.rifle.equip");
    private static final RawAnimation UNEQUIP_ANIM = RawAnimation.begin().thenPlay("animation.rifle.unequip");

    private static final RawAnimation RELOAD_BAT_1 = RawAnimation.begin().thenPlay("animation.rifle.reload_bat_1");
    private static final RawAnimation RELOAD_BAT_2 = RawAnimation.begin().thenPlay("animation.rifle.reload_bat_2");

    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("animation.rifle.shoot");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int BASE_COLOR = 0x0A6EA5;

    public static float MAX_SIZE = 0.075f;
    public static float MAX_EFFECT_SIZE = 0.2f;
    public static int MAX_CHARGE_LEVEL = 120;

    public static final ItemStack ammoItem = new ItemStack(RBHItems.SINGULARITY_BATTERY.get());

    public FirearmMode mode;

    public SingularityRifle(Properties pProperties) {
        super(pProperties.stacksTo(1));
        mode = new FirearmMode(8, 8, null, null,
                5, 5, null, null, EQUIP_ANIM, UNEQUIP_ANIM,
                5,
                ammoItem,
                60
                );

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SingularityRifleRenderer renderer;

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SingularityRifleRenderer();
                }

                return this.renderer;
            }
        });
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int bat1Eng = FirearmDataUtils.getBattery1Energy(stack);
        int bat2Eng = FirearmDataUtils.getBattery2Energy(stack);
        int chargeLevel = FirearmDataUtils.getChargeLevel(stack);

        tooltip.add(Component.translatable("rifle.charge_level")
                .append(": ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(chargeLevel + "/" + MAX_CHARGE_LEVEL).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.translatable("rifle.bat1_charge")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(bat1Eng + "/" + SingularityBattery.MAX_ENERGY).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.translatable("rifle.bat2_charge")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(bat2Eng + "/" + SingularityBattery.MAX_ENERGY).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.empty());

        // === Show keybind hints ===
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("▶ ")
                    .append(Component.literal("[" + RBHClient.RELOAD_RIFLE.getTranslatedKeyMessage().getString() + "]"))
                    .append(" Reload")
                    .withStyle(ChatFormatting.YELLOW));

            tooltip.add(Component.literal("▶ ")
                    .append(Component.literal("[" + RBHClient.CHARGE_RIFLE.getTranslatedKeyMessage().getString() + "]"))
                    .append(" Charge")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Hold §eShift§r for controls").withStyle(ChatFormatting.DARK_GRAY));
        }

    }
    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, slotId, isSelected);
        if (!isSelected) {
            FirearmDataUtils.setHoldingAttackKey(itemStack, false);
        }

        if (entity instanceof LivingEntity living)
            this.mode.onTick(itemStack, living, isSelected);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        if (location instanceof ItemEntity e)
            return new SingularityRifleItemEntity(e);
        return null;
    }

    public boolean isEquiped(ItemStack stack, LivingEntity entity) {
        return FirearmDataUtils.isEquipped(stack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "main", 0, (state) -> state.setAndContinue(IDLE_ANIM_SPIN)),
                new AnimationController<>(this, "move", 0, (state) -> PlayState.STOP).triggerableAnim("equip", EQUIP_ANIM).triggerableAnim("unequip", UNEQUIP_ANIM).triggerableAnim("idle", IDLE_ANIM),
                new AnimationController<>(this, "reload", 0, (state) -> PlayState.STOP).triggerableAnim("reload1", RELOAD_BAT_1).triggerableAnim("reload2", RELOAD_BAT_2),
                new AnimationController<>(this, "shoot", 0, (state) -> {
                    if (state.getController().hasAnimationFinished())
                        state.resetCurrentAnimation();
                    return PlayState.STOP;
                }).triggerableAnim("shoot", SHOOT_ANIM)
        );
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return false; // Not a tool
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return isAiming(stack, player); // Cancel hit if aiming
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public boolean shouldBeColorful(ItemStack item) {
        String name = item.getDisplayName().getString();
        return name.equals("[jeb_]");
    }

    @Override
    public float getFov(ItemStack stack, Player player, float currentFovModifier, float partialTicks) {
        boolean isAiming = player.isUsingItem();
        float zoomIn = 0.5f; // TODO configurable by attachments, etc

        int denom = mode.isAiming(stack, player) ? mode.aimTime() : mode.unaimTime();
        float aimingTime = (float) denom - mode.getAimingTime(stack, player);
        float frac = denom > 0 ? aimingTime / (float) denom : 1;
        float frac1 = denom > 0 ? partialTicks / (float) denom : 0;
        float d = isAiming ? frac + frac1 : 1 - frac - frac1;
        d = Mth.clamp(d, 0f, 1f);
        float d1 = d * d * d;
        return Mth.lerp(d1, 1f, zoomIn) * currentFovModifier;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Nullable public Action getCurrentAction(ItemStack itemStack) { return FirearmDataUtils.getAction(itemStack); }

    public void onReload(ItemStack stack, LivingEntity entity) {
        mode.tryRunningReloadAction(stack, entity, FirearmMode.ReloadPhaseType.PREPARE, true, false);
    }

    public void chargeStart(ItemStack mainhandItem, ServerPlayer sender) {
        FirearmDataUtils.setCharging(mainhandItem, true);
    }

    public void chargeEnd(ItemStack mainhandItem, ServerPlayer sender) {
        FirearmDataUtils.setCharging(mainhandItem, false);
    }

    @Override
    public boolean isHoldingAttackKey(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.isHoldingAttackKey(itemStack);
    }

    @Override
    public boolean onPressAttackKey(ItemStack itemStack, LivingEntity entity) {
        FirearmDataUtils.setHoldingAttackKey(itemStack, true);

        return true;
    }

    @Override
    public void onReleaseAttackKey(ItemStack itemStack, LivingEntity entity) {
        FirearmDataUtils.setHoldingAttackKey(itemStack, false);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        this.stopAiming(itemStack, entity);
        return super.finishUsingItem(itemStack, level, entity);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int timeCharged) {
        this.stopAiming(itemStack, entity);
        super.releaseUsing(itemStack, level, entity, timeCharged);
    }

    public void stopAiming(ItemStack itemStack, LivingEntity entity) {
        mode.stopAiming(itemStack, entity);
    }

    public boolean isAiming(ItemStack itemStack, LivingEntity entity) {
        return mode.isAiming(itemStack, entity);
    }

    @Override public int getUseDuration(@NotNull ItemStack itemStack) { return 72000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean startUsing = false;
        if (mode.canAim(itemStack, player)) {
            mode.startAiming(itemStack, player);
            startUsing = true;
        }
        return startUsing ? ItemUtils.startUsingInstantly(level, player, hand) : super.use(level, player, hand);
    }

    public enum Action implements StringRepresentable {
        RELOAD(false),
        FIRING(true),
        CHARGE_START(true),
        CHARGE_END(true);

        private static final Map<String, Action> BY_ID = Arrays.stream(values())
                .collect(Collectors.toMap(Action::getSerializedName, Function.identity()));

        private final String id = this.name().toLowerCase(Locale.ROOT);

        private final boolean canAim;

        Action(boolean canAim) {
            this.canAim = canAim;
        }

        @Override public String getSerializedName() { return this.id; }

        @Nullable
        public static Action byId(String id) { return BY_ID.get(id); }

        public boolean canAim() { return this.canAim; }
    }
}
