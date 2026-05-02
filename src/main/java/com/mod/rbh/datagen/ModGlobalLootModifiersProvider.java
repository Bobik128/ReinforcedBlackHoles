package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries, ReinforcedBlackHoles.MODID);
    }

    @Override
    protected void start() {
        /*
        add(
                "example_modifier",
                new YourLootModifier(
                        new LootItemCondition[] {
                                LootItemRandomChanceCondition.randomChance(0.35f).build()
                        },
                        RBHItems.SOME_ITEM.get()
                )
        );
        */
    }
}