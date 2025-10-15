package com.mod.rbh.items.renderer;

import com.mod.rbh.client.RifleIcons;
import com.mod.rbh.compat.ShaderCompat;
import com.mod.rbh.entity.renderer.BlackHoleRenderer;
import com.mod.rbh.items.SingularityBattery;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import com.mod.rbh.sound.ItemLoopingSound;
import com.mod.rbh.sound.RBHSounds;
import com.mod.rbh.utils.FirearmDataUtils;
import com.mod.rbh.utils.FirearmMode;
import com.mod.rbh.utils.LightningRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mod.rbh.items.renderer.SingularityRifleModel.shootTriggered;

// TODO fix not rendering in hand when there is black hole
public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {

    private static boolean lastTimeShadersEnabled = false;
    public static Map<Long, ItemLoopingSound> sounds = new HashMap<>();
    private int cachedColor = 0x000000;

    public SingularityRifleRenderer() {
        super(new SingularityRifleModel());
        ((SingularityRifleModel) this.model).renderer = this;

        this.addRenderLayer(new PlayerArmsRenderLayer(this));
    }

    @Override
    public RenderType getRenderType(SingularityRifle animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }

    @Override
    public void preRender(PoseStack poseStack, SingularityRifle animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        model.getBone("L_ARM").ifPresent(b -> b.setHidden(true));
        model.getBone("R_ARM").ifPresent(b -> b.setHidden(true));

        cachedColor = FirearmDataUtils.getColor(currentItemStack);

        manageSound(poseStack);
    }

    private Vec3 worldPosFromPoseStack(PoseStack poseStack) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        // extract the translation from the PoseStack
        Matrix4f matrix = poseStack.last().pose();
        Vector4f origin = new Vector4f(0, 0, 0, 1);
        origin.mul(matrix);

        // the poseStack is in camera space, so add the camera position
        return new Vec3(origin.x(), origin.y(), origin.z()).add(camPos);
    }

    private void manageSound(PoseStack stack) {
        long id = GeoItem.getId(currentItemStack);
        ItemLoopingSound itemSound = sounds.get(id);
        Minecraft mc = Minecraft.getInstance();
        float volume = FirearmMode.getVolume(currentItemStack);
        if (itemSound == null || itemSound.isStopped()) {
            itemSound = new ItemLoopingSound(RBHSounds.ELECTRIC_BUZZ_MONO.get(), SoundSource.NEUTRAL, volume + 0.01f);
            mc.getSoundManager().play(itemSound);
        }
        itemSound.setVolume(volume);
        itemSound.enabled = true;

        itemSound.setPos(worldPosFromPoseStack(stack));
//        sounds.put(id, itemSound);
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

        if (isGlowingPart(bone.getName())) {
            if (((SingularityRifle) currentItemStack.getItem()).shouldBeColorful(currentItemStack)) {
                float[] rgb = BlackHoleRenderer.glowColor(System.currentTimeMillis(), 6.0f, 1.0f, 0.9f, 2.0f);
                red = rgb[0];
                green = rgb[1];
                blue = rgb[2];
            } else {
                red = (float) ((cachedColor >> 16) & 0xFF) / 255;
                green = (float) ((cachedColor >> 8) & 0xFF) / 255;
                blue = (float) (cachedColor & 0xFF) / 255;
            }
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

        if (bone.getName().equals("monitor")) renderText(poseStack, bufferSource, 0xF000F0, packedOverlay, animatable);

        poseStack.popPose();

        if (FirearmDataUtils.getChargeLevel(currentItemStack) <= 0) return;
        if (bone.getName().equals("blackHoleLocatorPre")) {
            if (
                    renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                            || renderPerspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            || renderPerspective == ItemDisplayContext.GROUND
                            || renderPerspective == ItemDisplayContext.FIXED
            ) {
                boolean isFirstPerson = renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

                float modifier = (float) FirearmDataUtils.getChargeLevel(currentItemStack) / SingularityRifle.MAX_CHARGE_LEVEL;

                boolean shadersEnabled = ShaderCompat.shadersEnabled();

                poseStack.pushPose();
                if (!shadersEnabled || !(renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) {
                    poseStack.translate(0, 0.3125f, -0.421f);
                    poseStack.translate(bone.getPosX() / 16, bone.getPosY() / 16, bone.getPosZ() / 16);
                    PostEffectRegistry.HoleEffectInstance holeEffectInstance = RifleHoleEffectInstanceHolder.getUniqueEffect();
                    if (holeEffectInstance != null)
                        BlackHoleRenderer.renderBlackHole(poseStack, holeEffectInstance, isFirstPerson ? PostEffectRegistry.RenderPhase.AFTER_ARM : PostEffectRegistry.RenderPhase.AFTER_LEVEL, packedLight, SingularityRifle.MAX_EFFECT_SIZE * modifier, SingularityRifle.MAX_SIZE * modifier, ((SingularityRifle) currentItemStack.getItem()).shouldBeColorful(currentItemStack), Color.YELLOW.getRGB(), 4.0f);
                }
                if (shadersEnabled && !lastTimeShadersEnabled) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("WARNING: oculus shaders are not fully compatible with Black holes! There may be some visual bugs"), false);
                }

                lastTimeShadersEnabled = shadersEnabled;

                poseStack.popPose();

            }
        }
    }

    private void renderText(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay, SingularityRifle animatable) {
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0, 0.43, 0.2);

        poseStack.pushPose();
        poseStack.translate(-0.011, 0, 0);

        float fontScale = 0.0026f;
        poseStack.scale(fontScale, -fontScale, fontScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        font.drawInBatch(
                Integer.toString(FirearmDataUtils.getChargeLevel(getCurrentItemStack())),
                0, 0,
                0xFFFFFF,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.034, 0, 0);

        float fontScale2 = 0.001f;
        poseStack.scale(fontScale2, -fontScale2, fontScale2);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        font.drawInBatch(
                "eng.",
                0, 0,
                0xFFFFFF,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.02, 0, 0.04);

        float iconScale = 0.0022f;
        poseStack.scale(iconScale, -iconScale, iconScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        RifleIcons.drawColoredIcon(poseStack, bufferSource, packedLight, overlay, getIconForEnergy(FirearmDataUtils.getBattery1Energy(currentItemStack), SingularityBattery.MAX_ENERGY));

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.005, 0, 0.04);

        poseStack.scale(iconScale, -iconScale, iconScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        RifleIcons.drawColoredIcon(poseStack, bufferSource, packedLight, overlay, getIconForEnergy(FirearmDataUtils.getBattery2Energy(currentItemStack), SingularityBattery.MAX_ENERGY));

        poseStack.popPose();

        poseStack.popPose();
    }

    private static RifleIcons.Icons getIconForEnergy(int nowEnergy, int maxEnergy) {
        float k = (float) nowEnergy / maxEnergy;

        if (k == 0f) {
            return RifleIcons.Icons.EMPTY;
        } else if (k <= 0.25f) {
            return RifleIcons.Icons.QUARTER;
        } else if (k <= 0.5f) {
            return RifleIcons.Icons.HALF;
        } else if (k <= 0.75f) {
            return RifleIcons.Icons.THREE_QUARTERS;
        } else if (k <= 1f) {
            return RifleIcons.Icons.FULL;
        } else return RifleIcons.Icons.WARNING;
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

    @Override
    public void postRender(PoseStack poseStack, SingularityRifle animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        var p = new LightningRenderUtil.Params();
        p.worldSpace = false;
        p.seed = (System.nanoTime() >> 16);
        p.recursionDepth = 2;

        float k = (float) FirearmDataUtils.getChargeLevel(currentItemStack) / SingularityRifle.MAX_CHARGE_LEVEL;

        long stackId = GeoItem.getId(currentItemStack);
        if (shootTriggered.containsKey(stackId)) {
            double startTick = shootTriggered.get(stackId).first;
            float modifier1 = shootTriggered.get(stackId).second;

            double nowTick = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
            if (nowTick - startTick < 3) k = modifier1;
        }

        if (k <= 0) return;

        p.widthStart = 0.03f * k;
        p.widthEnd = 0.01f * k;

        poseStack.pushPose();

        Optional<GeoBone> bone = model.getBone("rifle");
        bone.ifPresent((b) -> {
            poseStack.translate((b.getPivotX() + b.getPosX()) / 16, (b.getPivotY() + b.getPosY()) / 16, (b.getPivotZ() + b.getPosZ()) / 16);
            Quaternionf q = new Quaternionf();
            q.rotationYXZ(b.getRotY(), b.getRotX(), b.getRotZ());
            poseStack.mulPose(q);
            poseStack.translate(-(b.getPivotX()) / 16, -(b.getPivotY()) / 16, -(b.getPivotZ()) / 16);
        });

        Vector3f start = new Vector3f();
        Optional<GeoBone> bone1 = model.getBone("holeInjector");
        bone1.ifPresent((b) -> {
            start.set((b.getPivotX() + b.getPosX())/16, (b.getPivotY() + b.getPosY())/16, (b.getPivotZ() + b.getPosZ())/16);
        });

        Vector3f end = new Vector3f();
        Optional<GeoBone> bone2 = model.getBone("blackHoleLocatorPre");
        bone2.ifPresent((b) -> {
            end.set((b.getPivotX() + b.getPosX())/16, (b.getPivotY() + b.getPosY())/16, (b.getPivotZ() + b.getPosZ())/16);
        });

        LightningRenderUtil.renderLightning(poseStack, bufferSource, new Vec3(start), new Vec3(end), p);
        LightningRenderUtil.renderLightning(poseStack, bufferSource, new Vec3(start), new Vec3(end), p);

        poseStack.popPose();
    }
}
