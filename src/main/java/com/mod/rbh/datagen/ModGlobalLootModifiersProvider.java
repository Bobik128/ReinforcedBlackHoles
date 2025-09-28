package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, ReinforcedBlackHoles.MODID);
    }

    @Override
    protected void start() {
//        add("pine_cone_from_grass", new AddItemModifier(new LootItemCondition[] {
//                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build(),
//                LootItemRandomChanceCondition.randomChance(0.35f).build()}, ModItems.PINE_CONE.get()));
//
//        add("pine_cone_from_creeper", new AddItemModifier(new LootItemCondition[] {
//                new LootTableIdCondition.Builder(ResourceLocation.fromNamespaceAndPath("entities/creeper")).build() }, ModItems.PINE_CONE.get()));
//
//        add("metal_detector_from_jungle_temples", new AddItemModifier(new LootItemCondition[] {
//                new LootTableIdCondition.Builder(ResourceLocation.fromNamespaceAndPath("chests/jungle_temple")).build() }, ModItems.METAL_DETECTOR.get()));
//
//
//        add("metal_detector_from_suspicious_sand", new AddSusSandItemModifier(new LootItemCondition[] {
//                new LootTableIdCondition.Builder(ResourceLocation.fromNamespaceAndPath("archaeology/desert_pyramid")).build() }, ModItems.METAL_DETECTOR.get()));
    }
}
