package com.mod.rbh.items.renderer;

import com.mod.rbh.items.SingularityRifle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

public class PlayerArmsRenderLayer extends GeoRenderLayer<SingularityRifle> {
    public PlayerArmsRenderLayer(GeoRenderer<SingularityRifle> entityRendererIn) {
        super(entityRendererIn);
    }

    private static void translate(PoseStack poseStack, GeoBone bone) {
        RenderUtils.translateMatrixToBone(poseStack, bone.getParent());
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
    }

    @Override
    public void render(PoseStack poseStack, SingularityRifle animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        SingularityRifleRenderer renderer = (SingularityRifleRenderer) getRenderer();

        LocalPlayer plr = Minecraft.getInstance().player;
        if (renderer.getRenderPerspective() == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && plr != null) {
            EntityRenderDispatcher disp = Minecraft.getInstance().getEntityRenderDispatcher();
            PlayerRenderer playerRenderer = (PlayerRenderer) disp.getRenderer(plr);
            PlayerModel<AbstractClientPlayer> plrModel = playerRenderer.getModel();

            getGeoModel().getBone("R_ARM").ifPresent(bone -> {
                ModelPart arm = plrModel.rightArm;
                ModelPart sleeve = plrModel.rightSleeve;

                arm.resetPose();
                sleeve.resetPose();

                poseStack.pushPose();
//                RenderUtils.prepMatrixForBone(poseStack, getGeoModel().getBone("rifle").get());
//                RenderUtils.prepMatrixForBone(poseStack, bone);
//                RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);
                translate(poseStack, bone);

                poseStack.translate(-arm.x / 16, -arm.y / 16, -arm.z / 16);

                poseStack.mulPose(Axis.XP.rotationDegrees(-90));

                arm.render(poseStack, bufferSource.getBuffer(RenderType.entitySolid(plr.getSkinTextureLocation())), packedLight, OverlayTexture.NO_OVERLAY);
                sleeve.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(plr.getSkinTextureLocation())), packedLight, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            });

            getGeoModel().getBone("L_ARM").ifPresent(bone -> {
                ModelPart arm = plrModel.leftArm;
                ModelPart sleeve = plrModel.leftSleeve;

                arm.resetPose();
                sleeve.resetPose();

                poseStack.pushPose();
//                RenderUtils.prepMatrixForBone(poseStack, getGeoModel().getBone("rifle").get());
//                RenderUtils.prepMatrixForBone(poseStack, bone);
//                RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

                translate(poseStack, bone);

                poseStack.translate(-arm.x / 16, -arm.y / 16, -arm.z / 16);

                poseStack.mulPose(Axis.XP.rotationDegrees(-90));

                arm.render(poseStack, bufferSource.getBuffer(RenderType.entitySolid(plr.getSkinTextureLocation())), packedLight, OverlayTexture.NO_OVERLAY);
                sleeve.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(plr.getSkinTextureLocation())), packedLight, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            });
        }
    }
}
