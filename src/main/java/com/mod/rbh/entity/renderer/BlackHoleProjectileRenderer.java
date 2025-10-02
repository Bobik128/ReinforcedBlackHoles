package com.mod.rbh.entity.renderer;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.entity.BlackHoleProjectile;
import com.mod.rbh.shaders.FboGuard;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.awt.*;
import java.lang.Math;

public class BlackHoleProjectileRenderer<T extends BlackHoleProjectile> extends EntityRenderer<T> {
    private static Logger LOGGER = LogUtils.getLogger();

    public BlackHoleProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull T pEntity) {
        return null;
    }

    @Override
    public void render(@NotNull T entity, float pEntityYaw, float pPartialTick, @NotNull PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        if (entity.getEffectInstance() == null) return;
        BlackHoleRenderer.renderBlackHole(poseStack, entity.getEffectInstance(), PostEffectRegistry.RenderPhase.AFTER_LEVEL, pPackedLight, entity.getEffectSize(), entity.getSize(), entity.shouldBeRainbow(), entity.getColor(), entity.getEffectExponent());
    }
}
