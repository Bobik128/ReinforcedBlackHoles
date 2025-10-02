package com.mod.rbh.recipe;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<DyeRifleRecipe>> DYE_RIFLE =
            RECIPE_SERIALIZERS.register("dye_rifle",
                    () -> new SimpleCraftingRecipeSerializer<>(DyeRifleRecipe::new));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
