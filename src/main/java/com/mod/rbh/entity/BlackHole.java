package com.mod.rbh.entity;

import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class BlackHole extends Entity implements IBlackHole {

    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_SIZE =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_EXPONENT =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STRETCH_STRENGTH =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> STRETCH_DIR =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RAINBOW =
            SynchedEntityData.defineId(BlackHole.class, EntityDataSerializers.BOOLEAN);

    public static final int RENDER_DISTANCE = 120;
    public static final Logger LOGGER = LogUtils.getLogger();

    @OnlyIn(Dist.CLIENT) public PostEffectRegistry.HoleEffectInstance effectInstance;

    public BlackHole(Vec3 pos, Level level, float size, float effectSize, EntityType<? extends BlackHole> pEntityType) {
        this(pEntityType, level);
        this.setPos(pos);
        this.setSize(size);
        this.setEffectSize(effectSize);
    }

    public BlackHole(Vec3 pos, Level level, float size, float effectSize, boolean rainbow, EntityType<? extends BlackHole> pEntityType) {
        this(pos, level, size, effectSize, pEntityType);
        this.setRainbow(rainbow);
    }

    public BlackHole(EntityType<? extends BlackHole> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        if (this.level().isClientSide) clientInit();
        this.setNoGravity(true);
    }

    @OnlyIn(Dist.CLIENT)
    void clientInit() {
        effectInstance = PostEffectRegistry.HoleEffectInstance.createEffectInstance();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < RENDER_DISTANCE * RENDER_DISTANCE;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SIZE, 0.6f);
        this.entityData.define(EFFECT_SIZE, 1.6f);
        this.entityData.define(EFFECT_EXPONENT, 3.6f);
        this.entityData.define(STRETCH_DIR, new Vector3f(1.0f, 0.0f, 0.0f));
        this.entityData.define(STRETCH_STRENGTH, 0.0f);
        this.entityData.define(COLOR, 0xFFFF00);
        this.entityData.define(RAINBOW, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Size", getSize());
        tag.putFloat("EffectSize", getEffectSize());
    }

    @Override
    public PostEffectRegistry.HoleEffectInstance getEffectInstance() {
        return effectInstance;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Size")) {
            setSize(tag.getFloat("Size"));
        }
        if (tag.contains("EffectSize")) {
            setEffectSize(tag.getFloat("EffectSize"));
        }
    }

    public void setSize(float value) {
        this.entityData.set(SIZE, value);
    }

    public float getSize() {
        return this.entityData.get(SIZE);
    }

    public void setEffectSize(float value) {
        this.entityData.set(EFFECT_SIZE, value);
    }

    public float getEffectSize() {
        return this.entityData.get(EFFECT_SIZE);
    }

    public void setEffectExponent(float value) {
        this.entityData.set(EFFECT_EXPONENT, value);
    }

    public float getEffectExponent() {
        return this.entityData.get(EFFECT_EXPONENT);
    }

    @Override
    public void setStretchStrength(float value) {
        this.entityData.set(STRETCH_STRENGTH, value);
    }

    @Override
    public float getStretchStrength() {
        return this.entityData.get(STRETCH_STRENGTH);
    }

    @Override
    public void setStretchDir(Vector3f value) {
        this.entityData.set(STRETCH_DIR, value);
    }

    @Override
    public Vector3f getStretchDir() {
        return this.entityData.get(STRETCH_DIR);
    }

    public void setColor(int value) {
        this.entityData.set(COLOR, value);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public void setRainbow(boolean value) {
        this.entityData.set(RAINBOW, value);
    }

    public boolean shouldBeRainbow() {
        return this.entityData.get(RAINBOW);
    }
}
