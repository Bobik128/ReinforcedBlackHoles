package com.mod.rbh.datagen.recipe;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.items.RBHItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ConditionalRecipe.builder()
                .addCondition(new NotCondition(new ModLoadedCondition("create")))
                .addRecipe(writer -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, RBHItems.SINGULARITY_BATTERY.get())
                        .requires(RBHItems.SINGULARITY_BATTERY_EMPTY.get())
                        .requires(Items.NETHER_STAR)
                        .requires(Items.EXPERIENCE_BOTTLE)
                        .unlockedBy(getHasName(RBHItems.SINGULARITY_BATTERY_EMPTY.get()), has(RBHItems.SINGULARITY_BATTERY_EMPTY.get()))
                        .save(writer))
                // ResourceLocation IDs do not include the .json suffix.
                .build(pWriter, ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "singularity_battery"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, RBHItems.SINGULARITY_RIFLE.get())
                .pattern("NA ")
                .pattern("SES")
                .pattern("Bss")
                .define('S', Items.NETHER_STAR)
                .define('E', Items.DRAGON_EGG)
                .define('N', Items.NETHERITE_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .define('B', Items.NETHERITE_BLOCK)
                .define('s', RBHItems.SINGULARITY_BATTERY_EMPTY.get())
                .unlockedBy(getHasName(Items.DRAGON_EGG), has(Items.DRAGON_EGG))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RBHItems.SINGULARITY_BATTERY_EMPTY.get(), 2)
                .pattern("IRI")
                .pattern("N N")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .define('S', Items.SMOOTH_STONE_SLAB)
                .unlockedBy(getHasName(RBHItems.SINGULARITY_RIFLE.get()), has(RBHItems.SINGULARITY_RIFLE.get()))
                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, ingredients, category, result, experience, cookingTime, group, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, ingredients, category, result, experience, cookingTime, group, "_from_blasting");
    }

    protected static void oreCooking(
            Consumer<FinishedRecipe> consumer,
            RecipeSerializer<? extends AbstractCookingRecipe> serializer,
            List<ItemLike> ingredients,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            String group,
            String recipeName
    ) {
        for (ItemLike itemLike : ingredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemLike), category, result, experience, cookingTime, serializer)
                    .group(group)
                    .unlockedBy(getHasName(itemLike), has(itemLike))
                    .save(consumer, ReinforcedBlackHoles.MODID + ":" + getItemName(result) + recipeName + "_" + getItemName(itemLike));
        }
    }
}
