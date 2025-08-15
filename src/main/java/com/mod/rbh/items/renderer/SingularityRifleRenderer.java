package com.mod.rbh.items.renderer;

import com.mod.rbh.entity.renderer.BlackHoleRenderer;
import com.mod.rbh.entity.renderer.SphereMesh;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
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
    public RenderType getRenderType(SingularityRifle animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }

        @Override
    public void renderRecursively(PoseStack poseStack, SingularityRifle animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
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

                poseStack.pushPose();
                poseStack.translate(0, 0.3125f, -0.421f);
                poseStack.translate(bone.getPosX()/16, bone.getPosY()/16, bone.getPosZ()/16);
//                SphereMesh.render(poseStack, buffer, 0.1f, 10, 10, packedLight, packedOverlay);
                PostEffectRegistry.HoleEffectInstance holeEffectInstance = RifleHoleEffectInstanceHolder.getEffect(currentItemStack);
                if (holeEffectInstance != null)
                    BlackHoleRenderer.renderBlackHole(poseStack, holeEffectInstance, isFirstPerson ? PostEffectRegistry.RenderPhase.AFTER_ARM : PostEffectRegistry.RenderPhase.AFTER_LEVEL, packedLight, 0.08f, 0.03f);
                poseStack.popPose();

            }
        }

        if (bone.getName().toUpperCase().endsWith("_EMISSIVE")) {
            //TODO emmisive textures handling
        }
    }
}
