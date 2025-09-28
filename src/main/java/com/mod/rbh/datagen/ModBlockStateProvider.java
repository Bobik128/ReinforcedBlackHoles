package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ReinforcedBlackHoles.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }

    private void facingBlock(RegistryObject<Block> blockRegistryObject) {
        facingBlock(blockRegistryObject, cubeAll(blockRegistryObject.get()));
    }

    private void facingBlock(RegistryObject<Block> blockRegistryObject, ModelFile modelFile) {
        getVariantBuilder(blockRegistryObject.get()).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            int xRot = switch (facing) {
                case UP -> 0;
                case DOWN -> 180;
                case NORTH -> 90;
                case SOUTH -> 90;
                case EAST -> 90;
                case WEST -> 90;
            };

            int yRot = switch (facing) {
                case SOUTH -> 180;
                case EAST -> 90;
                case WEST -> 270;
                default -> 0;
            };

            return ConfiguredModel.builder()
                    .modelFile(modelFile)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private String name(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }
}
