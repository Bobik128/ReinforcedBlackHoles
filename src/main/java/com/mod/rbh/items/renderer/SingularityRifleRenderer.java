package com.mod.rbh.items.renderer;

import com.mod.rbh.entity.renderer.SphereMesh;
import com.mod.rbh.items.SingularityRifle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {
    public SingularityRifleRenderer() {
        super(new SingularityRifleModel());
    }

    @Override
    public void postRender(PoseStack poseStack, SingularityRifle animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        CoreGeoBone coreBone = this.getGeoModel().getAnimationProcessor().getBone("blackHoleLocator");
        if (coreBone instanceof GeoBone bone) {
            poseStack.pushPose();
            poseStack.mulPoseMatrix(bone.getLocalSpaceMatrix());
            SphereMesh.render(poseStack, bufferSource.getBuffer(RenderType.solid()), 0.3f, 16, 16, packedLight, OverlayTexture.NO_OVERLAY);

            Matrix4f mat = poseStack.last().pose();
            Vector3f pos = mat.getTranslation(new Vector3f());
            Quaternionf rot = mat.getNormalizedRotation(new Quaternionf());

            poseStack.popPose();
        }
    }
}
