package com.mod.rbh.datagen;

import com.mod.rbh.datagen.recipe.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {

    public static void register(IEventBus modBus) {
        modBus.addListener(DataGenerators::gatherData);
    }

    private static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server data
        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(packOutput, lookupProvider)
        );

        generator.addProvider(
                event.includeServer(),
                ModLootTableProvider.create(packOutput, lookupProvider)
        );

        ModBlockTagGenerator blockTagGenerator = generator.addProvider(
                event.includeServer(),
                new ModBlockTagGenerator(packOutput, lookupProvider, existingFileHelper)
        );

        generator.addProvider(
                event.includeServer(),
                new ModItemTagGenerator(
                        packOutput,
                        lookupProvider,
                        blockTagGenerator.contentsGetter(),
                        existingFileHelper
                )
        );

        generator.addProvider(
                event.includeServer(),
                new ModGlobalLootModifiersProvider(packOutput, lookupProvider)
        );

        generator.addProvider(
                event.includeServer(),
                new ModPoiTypeTagsProvider(packOutput, lookupProvider, existingFileHelper)
        );

        // Client assets
        generator.addProvider(
                event.includeClient(),
                new ModBlockStateProvider(packOutput, existingFileHelper)
        );

        generator.addProvider(
                event.includeClient(),
                new ModItemModelProvider(packOutput, existingFileHelper)
        );
    }
}