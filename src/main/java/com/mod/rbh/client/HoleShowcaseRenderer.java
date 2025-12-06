package com.mod.rbh.client;

import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import com.mod.rbh.entity.TestBlackHole;
import com.mod.rbh.utils.LightningRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HoleShowcaseRenderer implements BlockEntityRenderer<HoleShowcaseBlockEntity> {

    public HoleShowcaseRenderer(BlockEntityRendererProvider.Context context) {
        // context contains font renderer, block models, etc.
    }

    @Override
    public void render(HoleShowcaseBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        LightningRenderUtil.Params p = new LightningRenderUtil.Params();
        p.worldSpace = false;
        p.seed = (System.nanoTime() >> 16);
        p.recursionDepth = 3;
        p.widthStart = 0.07f;
        p.widthEnd = 0.06f;

        Vec3 center = entity.getBlockPos().getCenter();
        List<TestBlackHole> bhs = Minecraft.getInstance().level.getEntitiesOfClass(TestBlackHole.class, AABB.ofSize(center.add(0, 3, 0), 0.1, 5.0, 0.1));
        if (!bhs.isEmpty())
         LightningRenderUtil.renderLightning(poseStack, bufferSource, new Vec3(0, 0.4, 0), new Vec3(0, bhs.get(0).position().y - center.y, 0), p);

        poseStack.popPose();
    }
}

