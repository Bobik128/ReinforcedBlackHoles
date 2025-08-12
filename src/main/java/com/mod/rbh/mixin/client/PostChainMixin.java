package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IPostChain;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(PostChain.class)
public class PostChainMixin implements IPostChain {

    @Shadow
    @Final
    private List<PostPass> passes;

    @Shadow
    private float time;

    @Shadow
    private Matrix4f shaderOrthoMatrix;

    @Shadow
    private float lastStamp;

    @Unique
    @Override
    public List<PostPass> getPostPasses() {
        return passes;
    }

    @Unique
    @Override
    public void setUniformInPass(int pass, String uniformName, float x, float y, float z) {
        passes.get(pass).getEffect().safeGetUniform(uniformName).set(x, y, z);
    }

    @Unique
    @Override
    public void setUniformInPass(int pass, String uniformName, float x, float y) {
        passes.get(pass).getEffect().safeGetUniform(uniformName).set(x, y);
    }

    @Unique
    @Override
    public void setUniformInPass(int pass, String uniformName, float x) {
        passes.get(pass).getEffect().safeGetUniform(uniformName).set(x);
    }

    @Unique
    @Override
    public void setUniformInPass(int pass, String uniformName, Matrix4f matrix) {
        passes.get(pass).getEffect().safeGetUniform(uniformName).set(matrix);
    }

    @Override
    public void processPasses() {
        for(PostPass postpass : this.passes) {
            postpass.process(time / 20.0F);
        }
    }

    @Override
    public void setTimeStamp(float partialTicks) {
        if (partialTicks < this.lastStamp) {
            this.time += 1.0F - this.lastStamp;
            this.time += partialTicks;
        } else {
            this.time += partialTicks - this.lastStamp;
        }

        for(this.lastStamp = partialTicks; this.time > 20.0F; this.time -= 20.0F) {
        }
    }

    @Override
    public Matrix4f getOrtoMatrix() {
        return shaderOrthoMatrix;
    }
}
