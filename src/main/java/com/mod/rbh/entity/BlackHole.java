package com.mod.rbh.entity;

import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

public class BlackHole extends Projectile {
    public static final int RENDER_DISTANCE = 120;
    public static final Logger LOGGER = LogUtils.getLogger();

    @OnlyIn(Dist.CLIENT) public PostEffectRegistry.HoleEffectInstance effectInstance;

    public BlackHole(Vec3 pos, Level level) {
        this(RBHEntityTypes.BLACK_HOLE.get(), level);
        this.setPos(pos);
    }

    public BlackHole(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super((EntityType<? extends Projectile>) pEntityType, pLevel);
        noPhysics = true;
        if (this.level().isClientSide) clientInit();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientInit() {
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

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }
}
