package com.mod.rbh.entity.renderer;

import com.mod.rbh.client.StarburstRenderer;
import com.mod.rbh.entity.BlackHoleProjectile;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BlackHoleProjectileRenderer<T extends BlackHoleProjectile> extends EntityRenderer<T> {

    public BlackHoleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull T entity) {
        return null;
    }

    @Override
    public void render(
            @NotNull T entity,
            float entityYaw,
            float partialTick,
            @NotNull PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (entity.getEffectInstance() == null) {
            return;
        }

        int explodingTime = entity.getExplodingTime();

        if (explodingTime > -1) {
            poseStack.pushPose();
            poseStack.translate(0.0D, entity.getSize() / 2.0F, 0.0D);

            StarburstRenderer.renderStarburst(
                    poseStack,
                    buffer,
                    (explodingTime + partialTick) / (float) entity.maxExplodingTime,
                    entity.getColor(),
                    432L,
                    30,
                    80,
                    0.0F
            );

            poseStack.popPose();
        }

        BlackHoleRenderer.renderBlackHoleElliptical(
                poseStack,
                entity.getEffectInstance(),
                PostEffectRegistry.RenderPhase.AFTER_LEVEL,
                packedLight,
                entity.getEffectSize(),
                entity.getSize(),
                entity.shouldBeRainbow(),
                entity.getColor(),
                entity.getEffectExponent(),
                entity.getStretchDir(),
                entity.getStretchStrength()
        );
    }
}
