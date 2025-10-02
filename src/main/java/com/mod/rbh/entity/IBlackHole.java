package com.mod.rbh.entity;

import com.mod.rbh.shaders.PostEffectRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public interface IBlackHole {
    void setSize(float value);

    float getSize();

    void setEffectSize(float value);

    float getEffectSize();

    void setEffectExponent(float value);

    float getEffectExponent();

    void setColor(int value);

    int getColor();

    void setRainbow(boolean value);

    boolean shouldBeRainbow();

    PostEffectRegistry.HoleEffectInstance getEffectInstance();
}
