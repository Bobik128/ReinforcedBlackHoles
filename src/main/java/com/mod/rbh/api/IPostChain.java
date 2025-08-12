package com.mod.rbh.api;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

public interface IPostChain {
    static IPostChain fromPostChain(@NotNull PostChain postChain) {
        return (IPostChain) postChain;
    }

    List<PostPass> getPostPasses();

    void setUniformInPass(int pass, String uniformName, float x, float y, float z);
    void setUniformInPass(int pass, String uniformName, float x, float y);
    void setUniformInPass(int pass, String uniformName, float x);
    void setUniformInPass(int pass, String uniformName, Matrix4f matrix);

    void processPasses();
    void setTimeStamp(float partialTicks);

    Matrix4f getOrtoMatrix();
}
