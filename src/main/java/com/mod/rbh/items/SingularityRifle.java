package com.mod.rbh.items;

import com.mod.rbh.api.FovModifyingItem;
import com.mod.rbh.api.HoldAttackKeyInteraction;
import com.mod.rbh.items.renderer.SingularityRifleRenderer;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.utils.FirearmDataUtils;
import com.mod.rbh.utils.FirearmMode;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.example.registry.SoundRegistry;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingularityRifle extends Item implements GeoItem, FovModifyingItem, HoldAttackKeyInteraction {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("animation.rifle.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FirearmMode mode;
    public final PostEffectRegistry.HoleEffectInstance effectInstance;

    public SingularityRifle(Properties pProperties) {
        super(pProperties);
        mode = new FirearmMode(10, 10, null, null,
                10, 5, null, null
                );
        effectInstance = PostEffectRegistry.HoleEffectInstance.createEffectInstance();
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

    public boolean isEquiped(ItemStack stack, LivingEntity entity) {
        return FirearmDataUtils.isEquipped(stack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "main", 0, (state) -> state.setAndContinue(IDLE_ANIM))
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

    public void charge(ItemStack mainhandItem, ServerPlayer sender) {
        sender.displayClientMessage(Component.literal("pressed charge key on server"), true);
    }

    @Override
    public boolean isHoldingAttackKey(ItemStack itemStack, LivingEntity entity) {
        return FirearmDataUtils.isHoldingAttackKey(itemStack);
    }

    @Override
    public boolean onPressAttackKey(ItemStack itemStack, LivingEntity entity) {
        FirearmDataUtils.setHoldingAttackKey(itemStack, true);
        if (entity instanceof Player plr) {
            plr.displayClientMessage(Component.literal("pressed attack key | client: " + entity.level().isClientSide), !entity.level().isClientSide);
        }

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
        CHARGE(true),
        RELOADING(true),
        DRAW(false),
        COOLDOWN(false);

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
