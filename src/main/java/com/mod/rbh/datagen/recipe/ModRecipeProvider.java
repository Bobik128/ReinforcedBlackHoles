package com.mod.rbh.datagen.recipe;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    /*
    private static final List<ItemLike> ZIRCON_SMELTABLES = List.of(
            RBHItems.RAW_ZIRCON.get(),
            RBHBlocks.ZIRCON_ORE.get(),
            RBHBlocks.DEEPSLATE_ZIRCON_ORE.get()
    );
    */

    public ModRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        /*
        Example:

        oreSmelting(
                output,
                ZIRCON_SMELTABLES,
                RecipeCategory.MISC,
                RBHItems.ZIRCON.get(),
                0.25f,
                200,
                "zircon"
        );

        oreBlasting(
                output,
                ZIRCON_SMELTABLES,
                RecipeCategory.MISC,
                RBHItems.ZIRCON.get(),
                0.25f,
                100,
                "zircon"
        );
        */
    }
    protected static void oreSmelting(
            RecipeOutput output,
            List<ItemLike> ingredients,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            String group
    ) {
        oreCooking(
                output,
                RecipeSerializer.SMELTING_RECIPE,
                SmeltingRecipe::new,
                ingredients,
                category,
                result,
                experience,
                cookingTime,
                group,
                "_from_smelting"
        );
    }

    protected static void oreBlasting(
            RecipeOutput output,
            List<ItemLike> ingredients,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            String group
    ) {
        oreCooking(
                output,
                RecipeSerializer.BLASTING_RECIPE,
                BlastingRecipe::new,
                ingredients,
                category,
                result,
                experience,
                cookingTime,
                group,
                "_from_blasting"
        );
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(
            RecipeOutput output,
            RecipeSerializer<T> cookingSerializer,
            AbstractCookingRecipe.Factory<T> factory,
            List<ItemLike> ingredients,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            String group,
            String recipeName
    ) {
        for (ItemLike itemLike : ingredients) {
            SimpleCookingRecipeBuilder.generic(
                            Ingredient.of(itemLike),
                            category,
                            result,
                            experience,
                            cookingTime,
                            cookingSerializer,
                            factory
                    )
                    .group(group)
                    .unlockedBy(getHasName(itemLike), has(itemLike))
                    .save(
                            output,
                            ReinforcedBlackHoles.MODID + ":"
                                    + getItemName(result)
                                    + recipeName
                                    + "_"
                                    + getItemName(itemLike)
                    );
        }
    }
}