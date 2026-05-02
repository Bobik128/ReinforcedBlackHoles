package com.mod.rbh.datagen.loot;

import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    private final HolderLookup.Provider registries;

    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        this.registries = registries;
    }

    @Override
    protected void generate() {
        this.dropSelf(RBHBlocks.HOLE_SHOWCASE.get());

        /*
        Example ore drops in 1.21.1:

        this.add(
                RBHBlocks.SOME_ORE.get(),
                block -> createCopperLikeOreDrops(
                        RBHBlocks.SOME_ORE.get(),
                        RBHItems.SOME_RAW_ITEM.get()
                )
        );
        */
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block block, Item item) {
        HolderLookup.RegistryLookup<Enchantment> enchantments =
                registries.lookupOrThrow(Registries.ENCHANTMENT);

        return createSilkTouchDispatchTable(
                block,
                this.applyExplosionDecay(
                        block,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(
                                        enchantments.getOrThrow(Enchantments.FORTUNE)
                                ))
                )
        );
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return RBHBlocks.BLOCKS.getEntries()
                .stream()
                .map(holder -> (Block) holder.value())
                ::iterator;
    }
}