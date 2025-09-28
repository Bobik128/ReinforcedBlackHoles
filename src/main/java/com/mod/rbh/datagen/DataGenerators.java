package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.datagen.recipe.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {// BEFORE RUNNING DATAGEN, YOU NEED TO SET VOICECHAT TO COMPILE ONLY!!!!
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));// recipes
//        generator.addProvider(event.includeServer(), ModLootTableProvider.create(packOutput));// loot tables

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));// block states
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));// item models

        ModBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
                new ModBlockTagGenerator(packOutput, lookupProvider, existingFileHelper));// block tags
        generator.addProvider(event.includeServer(),
                new ModItemTagGenerator(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), existingFileHelper));// item tags

        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput));// loot modifiers
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(packOutput, lookupProvider, existingFileHelper));// poi type tags
    }
}
