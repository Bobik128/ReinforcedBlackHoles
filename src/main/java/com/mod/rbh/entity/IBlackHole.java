package com.mod.rbh.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public interface IBlackHole {
    void setSize(float value);

    float getSize();

    void setEffectSize(float value);

    float getEffectSize();

    void setRainbow(boolean value);

    boolean shouldBeRainbow();
}
