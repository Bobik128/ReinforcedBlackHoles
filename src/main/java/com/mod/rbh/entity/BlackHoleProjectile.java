package com.mod.rbh.entity;

import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

public class BlackHoleProjectile extends Projectile implements IBlackHole {
    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_SIZE =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> RAINBOW =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.BOOLEAN);

    public static final int RENDER_DISTANCE = 120;
    public static final Logger LOGGER = LogUtils.getLogger();

    public int life = 0;
    public int lifetime = 1000;

    @OnlyIn(Dist.CLIENT) public PostEffectRegistry.HoleEffectInstance effectInstance;

    public BlackHoleProjectile(Vec3 pos, Level level, float size, float effectSize) {
        this(RBHEntityTypes.BLACK_HOLE_PROJECTILE.get(), level);
        this.setPos(pos);
        this.setSize(size);
        this.setEffectSize(effectSize);
    }

    public BlackHoleProjectile(Vec3 pos, Level level, float size, float effectSize, boolean rainbow) {
        this(pos, level, size, effectSize);
        this.setRainbow(rainbow);
    }

    public BlackHoleProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        if (this.level().isClientSide) clientInit();
        this.setNoGravity(true);
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
        this.entityData.define(SIZE, 0.5f);
        this.entityData.define(EFFECT_SIZE, 2.0f);
        this.entityData.define(RAINBOW, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Size", getSize());
        tag.putFloat("EffectSize", getEffectSize());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Size")) {
            setSize(tag.getFloat("Size"));
        }
        if (tag.contains("EffectSize")) {
            setEffectSize(tag.getFloat("EffectSize"));
        }
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return life > 2 && super.shouldRender(pX, pY, pZ);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();

        Vec3 vec33 = this.getDeltaMovement();
        this.move(MoverType.SELF, vec33);
        this.setDeltaMovement(vec33);

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (!this.noPhysics) {
            this.onHit(hitresult);
            this.hasImpulse = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
        }

        ++this.life;

        if (!this.level().isClientSide && this.life > this.lifetime) {
            this.explode();
        }

    }

    /**
     * Called when the arrow hits an entity
     */
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            this.explode();
        }
    }

    protected void onHitBlock(BlockHitResult pResult) {
        BlockPos blockpos = new BlockPos(pResult.getBlockPos());
        this.level().getBlockState(blockpos).entityInside(this.level(), blockpos, this);
        if (!this.level().isClientSide()) {
            this.explode();
        }

        super.onHitBlock(pResult);
    }

    private void explode() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 8.0F * this.getSize() / SingularityRifle.MAX_SIZE, Level.ExplosionInteraction.TNT);
        this.discard();
    }


    private void dealExplosionDamage() {
        float f = 5.0F;

        if (f > 0.0F) {

            double d0 = 5.0D;
            Vec3 vec3 = this.position();

            for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                if (!(this.distanceToSqr(livingentity) > 25.0D)) {
                    boolean flag = false;

                    for(int i = 0; i < 2; ++i) {
                        Vec3 vec31 = new Vec3(livingentity.getX(), livingentity.getY(0.5D * (double)i), livingentity.getZ());
                        HitResult hitresult = this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                        if (hitresult.getType() == HitResult.Type.MISS) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        float f1 = f * (float)Math.sqrt((5.0D - (double)this.distanceTo(livingentity)) / 5.0D);
                        livingentity.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), f1);
                    }
                }
            }
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

    public void setRainbow(boolean value) {
        this.entityData.set(RAINBOW, value);
    }

    public boolean shouldBeRainbow() {
        return this.entityData.get(RAINBOW);
    }

}
