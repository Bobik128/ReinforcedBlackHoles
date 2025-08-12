package com.mod.rbh.entity;

import com.mod.rbh.shaders.BlitPostPass;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlackHole extends Entity {
    public static final int RENDER_DISTANCE = 120;
    public static final Logger LOGGER = LogUtils.getLogger();

    @OnlyIn(Dist.CLIENT) public RenderTarget finalTarget;
    @OnlyIn(Dist.CLIENT) public RenderTarget swapTarget;
    @OnlyIn(Dist.CLIENT) public PostPass holePass;
    @OnlyIn(Dist.CLIENT) public PostEffectRegistry.HoleEffectInstance effectInstance;

    public BlackHole(Vec3 pos, Level level) {
        this(RBHEntityTypes.BLACK_HOLE.get(), level);
        this.setPos(pos);
    }

    public BlackHole(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        noPhysics = true;
        if (this.level().isClientSide) clientInit();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientInit() {
        Window window = Minecraft.getInstance().getWindow();
        finalTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
        swapTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);

        try {
            holePass = new BlitPostPass(Minecraft.getInstance().getResourceManager(), "rbh:black_hole", finalTarget, swapTarget);
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }

        if (holePass != null) {
            holePass.addAuxAsset("MainSampler", Minecraft.getInstance().getMainRenderTarget()::getColorTextureId, window.getWidth(), window.getHeight());
        }
        effectInstance = new PostEffectRegistry.HoleEffectInstance(new ArrayList<>(List.of(holePass)), null, finalTarget, 0.0f);
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
