package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ReinforcedBlackHoles.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        /*
        Example:

        simpleItem(RBHItems.SOME_ITEM);
        */
    }

    private ItemModelBuilder simpleItem(DeferredItem<? extends Item> item) {
        return withExistingParent(
                item.getId().getPath(),
                ResourceLocation.parse("item/generated")
        ).texture(
                "layer0",
                ResourceLocation.fromNamespaceAndPath(
                        ReinforcedBlackHoles.MODID,
                        "item/" + item.getId().getPath()
                )
        );
    }
}