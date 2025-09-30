package com.mod.rbh.datagen.recipe;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.compat.CreateCompat;
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
//    private static final List<ItemLike> ZIRCON_SMELTABLES = List.of(RBHItems.RAW_ZIRCON.get(),
//            ModBlocks.ZIRCON_ORE.get(), ModBlocks.DEEPSLATE_ZIRCON_ORE.get());

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
                .build(pWriter, ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "singularity_battery.json"));

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

//        oreSmelting(pWriter, ZIRCON_SMELTABLES, RecipeCategory.MISC, ModItems.ZIRCON.get(), 0.25f, 200, "ZIRCON");
//        oreBlasting(pWriter, ZIRCON_SMELTABLES, RecipeCategory.MISC, ModItems.ZIRCON.get(), 0.25f, 100, "ZIRCON");
//
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ZIRCON_BLOCK.get())
//                .pattern("SSS")
//                .pattern("SSS")
//                .pattern("SSS")
//                .define('S', ModItems.ZIRCON.get())
//                .unlockedBy(getHasName(ModItems.ZIRCON.get()), has(ModItems.ZIRCON.get()))
//                .save(pWriter);
//
//        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ZIRCON.get(), 9)
//                .requires(ModBlocks.ZIRCON_BLOCK.get())
//                .unlockedBy(getHasName(ModBlocks.ZIRCON_BLOCK.get()), has(ModBlocks.ZIRCON_BLOCK.get()))
//                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                            pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer, ReinforcedBlackHoles.MODID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
