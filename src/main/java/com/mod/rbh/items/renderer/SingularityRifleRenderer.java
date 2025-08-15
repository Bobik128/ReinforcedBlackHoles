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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Optional;

public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {
    public SingularityRifleRenderer() {
        super(new SingularityRifleModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, SingularityRifle animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("blackHoleLocator")) {
            if (
                    renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.GROUND
            ) {
                poseStack.pushPose();
                SphereMesh.render(poseStack, buffer, 0.1f, 10, 10, packedLight, packedOverlay);
//                BlackHoleRenderer.renderBlackHole(poseStack, RifleHoleEffectInstanceHolder.getEffect(currentItemStack), bufferSource, packedLight);
                poseStack.popPose();
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
//
//    @Override
//    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
//        if (
//                transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
//                || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
//                || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
//                || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
//                || transformType == ItemDisplayContext.GROUND
//        ) {
//
//            Optional<GeoBone> coreBone = this.getGeoModel().getBone("blackHoleLocator");
//            Optional<GeoBone> mainCoreBone = this.getGeoModel().getBone("rifle");
//            if (coreBone.isPresent() && mainCoreBone.isPresent()) {
//                GeoBone bone = coreBone.get();
//                GeoBone mainBone = mainCoreBone.get();
//                poseStack.pushPose();
//
//                RenderUtils.prepMatrixForBone(poseStack, mainBone);
//                RenderUtils.prepMatrixForBone(poseStack, bone);
//
//                BlackHoleRenderer.renderBlackHole(poseStack, RifleHoleEffectInstanceHolder.getEffect(stack), bufferSource, packedLight);
//
//                poseStack.popPose();
//            }
//        }
//        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
//    }
}
