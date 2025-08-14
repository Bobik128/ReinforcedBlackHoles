package com.mod.rbh.items.renderer;

import com.mod.rbh.entity.renderer.BlackHoleRenderer;
import com.mod.rbh.entity.renderer.SphereMesh;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Optional;

public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {
    public SingularityRifleRenderer() {
        super(new SingularityRifleModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, SingularityRifle animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (bone.getName().equals("blackHoleLocator")) {

        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        if (
                transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.GROUND
        ) {

            Optional<GeoBone> coreBone = this.getGeoModel().getBone("blackHoleLocator");
            if (coreBone.isPresent()) {
                GeoBone bone = coreBone.get();
                poseStack.pushPose();
                poseStack.translate(bone.getPivotX() / 16, bone.getPivotY() / 16, bone.getPivotZ() / 16);
//            SphereMesh.render(poseStack, buffer, 0.1f, 16, 16, packedLight, OverlayTexture.NO_OVERLAY);
                BlackHoleRenderer.renderBlackHole(poseStack, RifleHoleEffectInstanceHolder.getEffect(stack), bufferSource, packedLight);
                poseStack.popPose();
            }
        }
    }
}
