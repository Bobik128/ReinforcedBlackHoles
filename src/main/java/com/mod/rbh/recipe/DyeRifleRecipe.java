package com.mod.rbh.recipe;

import com.mod.rbh.items.RBHItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DyeRifleRecipe extends CustomRecipe {
    public DyeRifleRecipe(ResourceLocation id, CraftingBookCategory cat) { super(id, cat); }

    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level level) {
        boolean sawBase = false, sawDye = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(RBHItems.SINGULARITY_RIFLE.get())) { if (sawBase) return false; sawBase = true; }
            else if (s.is(Tags.Items.DYES))    { if (sawDye) return false; sawDye = true; }
            else return false;
        }
        return sawBase && sawDye;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv, @NotNull RegistryAccess regs) {
        ItemStack base = ItemStack.EMPTY;
        ItemStack dye  = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(RBHItems.SINGULARITY_RIFLE.get())) base = s.copy();
            else if (s.is(Tags.Items.DYES))   dye  = s;
        }
        if (base.isEmpty() || dye.isEmpty()) return ItemStack.EMPTY;

        // Map the dye to RGB int
        int rgb = rgbFromDye(dye);

        // Write either legacy NBT int or 1.20.5+ data component
        base.getOrCreateTag().putInt("Color", rgb);
        return base;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return w * h >= 2; }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return RBHRecipes.DYE_RIFLE.get(); }

    private static int rgbFromDye(ItemStack dyeStack) {
        // Vanilla dye → DyeColor
        DyeColor dc = DyeColor.getColor(dyeStack);
        return (dc != null ? dc.getTextColor() : 0xFFFFFF);
    }

    private static final Map<DyeColor,Integer> VANILLA_RGB = Map.ofEntries(
            Map.entry(DyeColor.WHITE,   0xF9FFFE),
            Map.entry(DyeColor.ORANGE,  0xF9801D),
            Map.entry(DyeColor.MAGENTA, 0xC74EBD),
            Map.entry(DyeColor.LIGHT_BLUE, 0x3AB3DA),
            Map.entry(DyeColor.YELLOW,  0xFED83D),
            Map.entry(DyeColor.LIME,    0x80C71F),
            Map.entry(DyeColor.PINK,    0xF38BAA),
            Map.entry(DyeColor.GRAY,    0x474F52),
            Map.entry(DyeColor.LIGHT_GRAY, 0x9D9D97),
            Map.entry(DyeColor.CYAN,    0x169C9C),
            Map.entry(DyeColor.PURPLE,  0x8932B8),
            Map.entry(DyeColor.BLUE,    0x3C44AA),
            Map.entry(DyeColor.BROWN,   0x835432),
            Map.entry(DyeColor.GREEN,   0x5E7C16),
            Map.entry(DyeColor.RED,     0xB02E26),
            Map.entry(DyeColor.BLACK,   0x1D1D21)
    );
}

