package com.mod.rbh.datagen;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ReinforcedBlackHoles.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        facingBlockCustom(RBHBlocks.HOLE_SHOWCASE, "hole_showcase");
    }

    private void facingBlock(DeferredBlock<? extends Block> block) {
        facingBlock(block, cubeAll(block.get()));
    }

    private void facingBlockCustomAllSides(DeferredBlock<? extends Block> block) {
        ModelFile model = models().getExistingFile(
                ResourceLocation.fromNamespaceAndPath(
                        ReinforcedBlackHoles.MODID,
                        "block/" + name(block.get())
                )
        );

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);

            int xRot = switch (facing) {
                case UP -> 0;
                case DOWN -> 180;
                case NORTH, SOUTH, EAST, WEST -> 90;
            };

            int yRot = switch (facing) {
                case SOUTH -> 180;
                case EAST -> 90;
                case WEST -> 270;
                default -> 0;
            };

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
    }

    private void facingBlockCustom(DeferredBlock<? extends Block> block, String modelName) {
        ModelFile model = models().getExistingFile(
                ResourceLocation.fromNamespaceAndPath(
                        ReinforcedBlackHoles.MODID,
                        "block/" + modelName
                )
        );

        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            int yRot = switch (facing) {
                case SOUTH -> 180;
                case WEST -> 270;
                case EAST -> 90;
                default -> 0;
            };

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(yRot)
                    .build();
        });

        itemModels()
                .getBuilder(block.getId().getPath())
                .parent(model);
    }

    private void facingBlock(DeferredBlock<? extends Block> block, ModelFile modelFile) {
        getVariantBuilder(block.get()).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);

            int xRot = switch (facing) {
                case UP -> 0;
                case DOWN -> 180;
                case NORTH, SOUTH, EAST, WEST -> 90;
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

    private void blockWithItem(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    private String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}