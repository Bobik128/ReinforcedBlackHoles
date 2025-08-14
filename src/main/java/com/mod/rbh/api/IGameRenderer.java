package com.mod.rbh.api;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IGameRenderer {
    @OnlyIn(Dist.CLIENT)
    static IGameRenderer get() {
        return (IGameRenderer) Minecraft.getInstance().gameRenderer;
    }

    float getFovPublic();
}
