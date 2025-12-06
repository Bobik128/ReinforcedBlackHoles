package com.mod.rbh.blocks.custom.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class HoleShowcaseBlockEntity extends BlockEntity {
    protected static final Logger LOGGER = LogUtils.getLogger();

    public HoleShowcaseBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RBHBlockEntities.HOLE_SHOWCASE_BE.get(), pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

    }
}
