package com.mod.rbh.compat;

import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

public class CreateCompat implements IConditionBuilder {
    public static final String CREATE_MODID = "create";
    private static boolean isCreateLoaded = false;

    public static boolean isCreateLoaded() {
        return isCreateLoaded;
    }

    public static void init() {
        isCreateLoaded = ModList.get().isLoaded(CREATE_MODID);
    }
}
