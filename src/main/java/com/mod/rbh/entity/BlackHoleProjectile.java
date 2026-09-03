package com.mod.rbh.entity;

import com.mod.rbh.CommonConfig;
import com.mod.rbh.RegisterDamageTypes;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.sound.RBHSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackHoleProjectile extends Projectile implements IBlackHole {

    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_SIZE =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EFFECT_EXPONENT =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STRETCH_STRENGTH =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> STRETCH_DIR =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RAINBOW =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EXPLODING_TIME =
            SynchedEntityData.defineId(BlackHoleProjectile.class, EntityDataSerializers.INT);

    public static final int RENDER_DISTANCE = 120;

    /**
     * Ported from the final Forge 1.20.1 Singularity Rifle implementation.
     * At the rifle's MAX_SIZE this makes a direct hit effectively lethal.
     */
    public static final float DAMAGE_SIZE_MULTIPLIER = 10_000.0F / SingularityRifle.MAX_SIZE;
    protected static final float MAX_ITEM_REMOVE_PERCENT = 0.6F;

    public static final Logger LOGGER = LogUtils.getLogger();

    public int life = 0;
    public int lifetime = 1000;
    public final int maxExplodingTime = 3;

    private Vec3 lastDeltaDir = new Vec3(1.0D, 0.0D, 0.0D);

    @OnlyIn(Dist.CLIENT)
    public PostEffectRegistry.HoleEffectInstance effectInstance;

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

    public BlackHoleProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void initEffectInstance() {
        if (effectInstance == null && Minecraft.getInstance().isSameThread()) {
            effectInstance = PostEffectRegistry.HoleEffectInstance.createEffectInstance();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void releaseEffectInstance() {
        if (effectInstance != null) {
            PostEffectRegistry.releaseHole(effectInstance);
            effectInstance = null;
        }
    }

    @Override
    public void onRemovedFromWorld() {
        if (level().isClientSide) {
            releaseEffectInstance();
        }

        super.onRemovedFromWorld();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < RENDER_DISTANCE * RENDER_DISTANCE;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SIZE, 0.5F);
        this.entityData.define(EFFECT_SIZE, 2.0F);
        this.entityData.define(EFFECT_EXPONENT, 4.0F);
        this.entityData.define(STRETCH_DIR, new Vector3f(1.0F, 0.0F, 0.0F));
        this.entityData.define(STRETCH_STRENGTH, 0.0F);
        this.entityData.define(COLOR, 0xFFFF00);
        this.entityData.define(RAINBOW, false);
        this.entityData.define(EXPLODING_TIME, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Size", getSize());
        tag.putFloat("EffectSize", getEffectSize());
        tag.putInt("ExplodingTime", getExplodingTime());
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

        if (tag.contains("ExplodingTime")) {
            setExplodingTime(tag.getInt("ExplodingTime"));
        }
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return (life > 1 || getExplodingTime() >= 0) && super.shouldRender(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            initEffectInstance();
        }

        /*
         * EXPLODING_TIME is synchronized entity data, so both sides agree that
         * the projectile has stopped moving while the starburst is displayed.
         */
        if (getExplodingTime() >= 0) {
            setDeltaMovement(Vec3.ZERO);

            if (!level().isClientSide) {
                int next = getExplodingTime() + 1;
                setExplodingTime(next);

                if (next > maxExplodingTime) {
                    discard();
                }
            }

            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        this.move(MoverType.SELF, velocity);
        this.setDeltaMovement(velocity);

        if (!this.level().isClientSide) {
            if (!lastDeltaDir.equals(velocity) && velocity.lengthSqr() > 1.0E-8D) {
                this.setStretchDir(velocity.toVector3f().normalize());
                this.setStretchStrength((float) velocity.length() * 3.0F);
            }

            lastDeltaDir = velocity;
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (!this.noPhysics) {
            this.onHit(hitResult);
            this.hasImpulse = true;
        }

        this.updateRotation();

        if (this.life == 0 && !this.isSilent()) {
            for (int i = 0; i < 2; i++) {
                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        RBHSounds.RIFLE_SHOOT.get(),
                        SoundSource.AMBIENT,
                        6.0F,
                        1.1F
                );
            }
        }

        ++this.life;

        if (!this.level().isClientSide && this.life > this.lifetime) {
            this.explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide || getExplodingTime() >= 0) {
            return;
        }

        Entity entity = result.getEntity();

        if (entity instanceof Player player) {
            float sizeFraction = this.getSize() / SingularityRifle.MAX_SIZE;
            removeItemsFromInventory(player, sizeFraction * MAX_ITEM_REMOVE_PERCENT);
        }

        entity.hurt(
                RegisterDamageTypes.causeHoleHitDamage(this),
                this.getEffectSize() * DAMAGE_SIZE_MULTIPLIER
        );

        this.explode();
    }

    private void removeItemsFromInventory(Player player, float fraction) {
        if (player.level().isClientSide) {
            return;
        }

        class SlotRef {
            private final List<ItemStack> list;
            private final int index;

            SlotRef(List<ItemStack> list, int index) {
                this.list = list;
                this.index = index;
            }

            ItemStack get() {
                return list.get(index);
            }

            void clear() {
                list.set(index, ItemStack.EMPTY);
            }
        }

        List<SlotRef> allSlots = new ArrayList<>();
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.items.size(); i++) {
            if (!inventory.items.get(i).isEmpty()) {
                allSlots.add(new SlotRef(inventory.items, i));
            }
        }

        for (int i = 0; i < inventory.armor.size(); i++) {
            if (!inventory.armor.get(i).isEmpty()) {
                allSlots.add(new SlotRef(inventory.armor, i));
            }
        }

        for (int i = 0; i < inventory.offhand.size(); i++) {
            if (!inventory.offhand.get(i).isEmpty()) {
                allSlots.add(new SlotRef(inventory.offhand, i));
            }
        }

        if (allSlots.isEmpty()) {
            return;
        }

        float clampedFraction = Mth.clamp(fraction, 0.0F, MAX_ITEM_REMOVE_PERCENT);
        Collections.shuffle(allSlots);

        int count = Math.min(allSlots.size(), Math.round(allSlots.size() * clampedFraction));
        var random = player.getRandom();
        Level level = player.level();

        for (int i = 0; i < count; i++) {
            SlotRef ref = allSlots.get(i);
            ItemStack stack = ref.get();

            if (stack.isEmpty()) {
                continue;
            }

            // Half of the removed stacks are thrown out; the other half are destroyed.
            if (random.nextBoolean()) {
                ItemEntity dropped = new ItemEntity(
                        level,
                        player.getX(),
                        player.getY() + 1.0D,
                        player.getZ(),
                        stack.copy()
                );

                dropped.setDeltaMovement(
                        (random.nextDouble() - 0.5D) * 0.8D,
                        random.nextDouble() * 0.6D + 0.2D,
                        (random.nextDouble() - 0.5D) * 0.8D
                );
                dropped.setPickUpDelay(20);
                level.addFreshEntity(dropped);
            }

            ref.clear();
        }

        player.getInventory().setChanged();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos blockPos = result.getBlockPos();
        this.level().getBlockState(blockPos).entityInside(this.level(), blockPos, this);

        if (!this.level().isClientSide && getExplodingTime() < 0) {
            this.explode();
        }

        super.onHitBlock(result);
    }

    private void explode() {
        if (this.level().isClientSide || getExplodingTime() >= 0) {
            return;
        }

        // Start the synchronized three-tick explosion/starburst state first.
        setExplodingTime(0);
        setDeltaMovement(Vec3.ZERO);

        this.level().broadcastEntityEvent(this, (byte) 17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());

        float strength = 8.0F * this.getSize() / SingularityRifle.MAX_SIZE;

        if (CommonConfig.destroyBlocks) {
            this.level().explode(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    strength,
                    Level.ExplosionInteraction.TNT
            );
        } else {
            dealExplosionDamage();

            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS,
                    4.0F,
                    1.0F
            );
        }
    }

    /**
     * Explosion-like entity damage without block destruction.
     */
    private void dealExplosionDamage() {
        if (this.level().isClientSide) {
            return;
        }

        float radius = 5.0F;
        double radiusSq = radius * radius;
        Vec3 center = this.position();

        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius)
        );

        for (LivingEntity target : targets) {
            double distanceSq = target.distanceToSqr(center);
            if (distanceSq > radiusSq) {
                continue;
            }

            double exposure = getExposure(center, target);
            if (exposure <= 0.0D) {
                continue;
            }

            double distance = Math.sqrt(distanceSq);
            double distanceFactor = 1.0D - distance / radius;
            float damage = (float) ((distanceFactor * exposure) * radius * 2.0D);

            DamageSource source;
            if (getOwner() instanceof LivingEntity livingOwner) {
                source = this.damageSources().mobProjectile(this, livingOwner);
            } else {
                source = this.damageSources().generic();
            }

            target.hurt(source, damage);
        }
    }

    private double getExposure(Vec3 explosionPos, Entity entity) {
        AABB box = entity.getBoundingBox();

        double stepX = 1.0D / ((box.getXsize() * 2.0D) + 1.0D);
        double stepY = 1.0D / ((box.getYsize() * 2.0D) + 1.0D);
        double stepZ = 1.0D / ((box.getZsize() * 2.0D) + 1.0D);

        double visible = 0.0D;
        double total = 0.0D;

        for (double x = 0.0D; x <= 1.0D; x += stepX) {
            for (double y = 0.0D; y <= 1.0D; y += stepY) {
                for (double z = 0.0D; z <= 1.0D; z += stepZ) {
                    Vec3 sample = new Vec3(
                            Mth.lerp(x, box.minX, box.maxX),
                            Mth.lerp(y, box.minY, box.maxY),
                            Mth.lerp(z, box.minZ, box.maxZ)
                    );

                    BlockHitResult hit = this.level().clip(
                            new ClipContext(
                                    sample,
                                    explosionPos,
                                    ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.NONE,
                                    this
                            )
                    );

                    if (hit.getType() == HitResult.Type.MISS) {
                        visible++;
                    }

                    total++;
                }
            }
        }

        return total <= 0.0D ? 0.0D : visible / total;
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

    public void setEffectExponent(float value) {
        this.entityData.set(EFFECT_EXPONENT, value);
    }

    public float getEffectExponent() {
        return this.entityData.get(EFFECT_EXPONENT);
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

    public void setExplodingTime(int value) {
        this.entityData.set(EXPLODING_TIME, value);
    }

    public int getExplodingTime() {
        return this.entityData.get(EXPLODING_TIME);
    }

    @Override
    public PostEffectRegistry.HoleEffectInstance getEffectInstance() {
        return effectInstance;
    }
}
