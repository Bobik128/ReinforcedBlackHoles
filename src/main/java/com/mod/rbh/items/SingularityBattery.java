package com.mod.rbh.items;

import com.mod.rbh.utils.FirearmDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SingularityBattery extends Item {
    public static final String ENERGY_TAG = "Energy";
    public static final int MAX_ENERGY = 600;

    public SingularityBattery(Properties props) {
        super(props.stacksTo(1)); // usually energy items don't stack
    }

    /* -------- NBT helpers -------- */
    public static int getEnergy(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ENERGY_TAG)) {
            setEnergy(stack, MAX_ENERGY);
        }
        return Math.max(0, Math.min(MAX_ENERGY, tag.getInt(ENERGY_TAG)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltip, @NotNull TooltipFlag pIsAdvanced) {

        int chargeLevel = getEnergy(stack);

        tooltip.add(Component.translatable("battery.charge_level")
                .append(": ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(chargeLevel + "/" + MAX_ENERGY).withStyle(ChatFormatting.WHITE)));

    }

    public static void setEnergy(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(ENERGY_TAG, Math.max(0, Math.min(MAX_ENERGY, value)));
    }

    public static int addEnergy(ItemStack stack, int delta) {
        int newVal = Math.max(0, Math.min(MAX_ENERGY, getEnergy(stack) + delta));
        setEnergy(stack, newVal);
        return newVal;
    }

    /* -------- Bar rendering -------- */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Show the bar whenever it's not empty or not full; change if you want always-visible
        int e = getEnergy(stack);
        return e > 0 && e < MAX_ENERGY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Vanilla bar width is 13px
        return Math.round(13.0f * getEnergy(stack) / (float) MAX_ENERGY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // White bar
        return 0xFFFFFF;
    }

    // changing item

    /** Call after any discharge to immediately enforce replacement (optional convenience). */
    public static void ensureDepletedSwap(ItemStack stack, @Nullable LivingEntity holder) {
        if (getEnergy(stack) <= 0) {
            ItemStack replacement = makeDepletedReplacement(stack);
            if (holder instanceof Player p) {
                // Replace all occurrences of THIS exact stack in the player inventory safely
                var inv = p.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    if (inv.getItem(i) == stack) { // reference-equal to avoid replacing other stacks
                        inv.setItem(i, replacement);
                        return;
                    }
                }
            }
        }
    }

    private static ItemStack makeDepletedReplacement(ItemStack old) {
        ItemStack out = new ItemStack(RBHItems.SINGULARITY_BATTERY_EMPTY.get()); // <- your target simple item
        // Optional: carry over name/lore/other safe tags
        if (old.hasCustomHoverName()) out.setHoverName(old.getHoverName());
        return out;
    }

    /** When the item sits in a player's inventory / is selected, this runs every tick. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && getEnergy(stack) <= 0) {
            if (entity instanceof Player p) {
                p.getInventory().setItem(slot, makeDepletedReplacement(stack));
            }
        }
    }

    /** When the item is dropped on the ground as an ItemEntity. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity itemEntity) {
        if (!itemEntity.level().isClientSide && getEnergy(stack) <= 0) {
            itemEntity.setItem(makeDepletedReplacement(stack));
        }
        return false; // keep default behavior
    }
}

