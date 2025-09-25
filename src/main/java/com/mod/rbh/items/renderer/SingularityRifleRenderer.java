package com.mod.rbh.items.renderer;

import com.mod.rbh.entity.renderer.BlackHoleRenderer;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import com.mod.rbh.utils.FirearmDataUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Objects;
// TODO fix not rendering in hand when there is black hole
public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {

    public SingularityRifleRenderer() {
        super(new SingularityRifleModel());
        ((SingularityRifleModel) this.model).renderer = this;

        this.addRenderLayer(new PlayerArmsRenderLayer(this));
    }

    @Override
    public RenderType getRenderType(SingularityRifle animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, SingularityRifle animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        model.getBone("L_ARM").ifPresent(b -> b.setHidden(true));
        model.getBone("R_ARM").ifPresent(b -> b.setHidden(true));
    }

    public ItemDisplayContext getRenderPerspective() {return renderPerspective;}

    @Override
    public void renderRecursively(PoseStack poseStack, SingularityRifle animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations));
        }

        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);

        ResourceLocation texture = this.getTextureLocation(this.animatable);
        RenderType renderTypeOverride = this.getRenderTypeOverrideForBone(bone, animatable, texture, bufferSource, partialTick);

        if (texture != null && renderTypeOverride == null) {
            renderTypeOverride = this.getRenderType(this.animatable, texture, bufferSource, partialTick);
        }

        if (renderTypeOverride != null) {
            buffer = bufferSource.getBuffer(renderTypeOverride);
        }

        super.renderCubesOfBone(poseStack, bone, buffer, isGlowingPart(bone.getName()) ? 0xF000F0 : packedLight, packedOverlay, red, green, blue, alpha);

        if (renderTypeOverride != null) {
            buffer = bufferSource.getBuffer(this.getRenderType(this.animatable, this.getTextureLocation(this.animatable), bufferSource, partialTick));
        }

        if (!isReRender) {
            this.applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }

        this.renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();

        if (FirearmDataUtils.getChargeLevel(currentItemStack) <= 0) return;
        if (bone.getName().equals("blackHoleLocatorPre")) {
            if (
                    renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.GROUND
            ) {
                boolean isFirstPerson = renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

                float modifier = (float) FirearmDataUtils.getChargeLevel(currentItemStack) / SingularityRifle.MAX_CHARGE_LEVEL;

                poseStack.pushPose();
                poseStack.translate(0, 0.3125f, -0.421f);
                poseStack.translate(bone.getPosX()/16, bone.getPosY()/16, bone.getPosZ()/16);
                PostEffectRegistry.HoleEffectInstance holeEffectInstance = RifleHoleEffectInstanceHolder.getUniqueEffect();
                if (holeEffectInstance != null)
                    BlackHoleRenderer.renderBlackHole(poseStack, holeEffectInstance, isFirstPerson ? PostEffectRegistry.RenderPhase.AFTER_ARM : PostEffectRegistry.RenderPhase.AFTER_LEVEL, packedLight, SingularityRifle.MAX_EFFECT_SIZE * modifier, SingularityRifle.MAX_SIZE * modifier, ((SingularityRifle)currentItemStack.getItem()).shouldBeColorful(currentItemStack));
                poseStack.popPose();

            }
        }

        if (bone.getName().toUpperCase().endsWith("_EMISSIVE")) {
            //TODO emmisive textures handling
        }
    }

    public static boolean isGlowingPartStatic(String name) {
        return name.toUpperCase().endsWith("_GLOWING") || name.toUpperCase().endsWith("_EMISSIVE");
    }

    public boolean isGlowingPart(String name) {
        return isGlowingPartStatic(name);
    }

    protected @javax.annotation.Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, SingularityRifle animatable, ResourceLocation texturePath, MultiBufferSource bufferSource, float partialTick) {
        if (bone != null && isGlowingPart(bone.getName())) {
            return RBHRenderTypes.getEmissiveRenderType(texturePath);
        }

        return null;
    }
}
