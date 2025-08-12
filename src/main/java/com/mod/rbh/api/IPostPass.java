package com.mod.rbh.api;

import net.minecraft.client.renderer.PostPass;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IPostPass {
    static IPostPass fromPostPass(@NotNull PostPass postChain) {
        return (IPostPass) postChain;
    }

    void toRunOnProcess(Consumer<PostPass> toRun);
}
