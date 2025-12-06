package com.mod.rbh.client.screen;

import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientScreenHandler {
    public static void openHoleShowcaseGui(BlockPos pos, HoleShowcaseBlockEntity.HoleShowcaseConfig config) {
        BlockEntity be = getClientLevel().getBlockEntity(pos);
        if (be instanceof HoleShowcaseBlockEntity hsbe) {
            Minecraft.getInstance().setScreen(new HoleShowcaseScreen(hsbe, config));
        }
    }

    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
