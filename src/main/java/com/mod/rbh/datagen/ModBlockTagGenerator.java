package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ReinforcedBlackHoles.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
//        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
//                .add(ModBlocks.KEYCODE_BLOCK.get());
//
//
//        this.tag(BlockTags.NEEDS_IRON_TOOL)
//                .add(ModBlocks.DEEPSLATE_ZIRCON_ORE.get(),
//                        ModBlocks.ZIRCON_ORE.get(),
//                        ModBlocks.ZIRCON_BLOCK.get());
    }
}
