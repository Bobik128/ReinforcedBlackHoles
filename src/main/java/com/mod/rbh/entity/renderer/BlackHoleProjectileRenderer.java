package com.mod.rbh.entity.renderer;

import com.mod.rbh.entity.BlackHoleProjectile;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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
        BlackHoleRenderer.renderBlackHoleElliptical(poseStack, entity.getEffectInstance(), PostEffectRegistry.RenderPhase.AFTER_LEVEL, pPackedLight, entity.getEffectSize(), entity.getSize(), entity.shouldBeRainbow(), entity.getColor(), entity.getEffectExponent(), entity.getStretchDir(), entity.getStretchStrength());
    }
}
