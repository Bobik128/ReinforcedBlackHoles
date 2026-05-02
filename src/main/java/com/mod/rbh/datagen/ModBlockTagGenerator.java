package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, ReinforcedBlackHoles.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(RBHBlocks.HOLE_SHOWCASE.get());

        /*
        Example:

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(RBHBlocks.SOME_BLOCK.get());
        */
    }
}