package com.mod.rbh.entity.renderer;

import com.mod.rbh.client.RBHCameraInfo;
import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.shaders.FboGuard;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4fStack;

import java.awt.Color;

public class BlackHoleRenderer<T extends BlackHole> extends EntityRenderer<T> {
    public static final ResourceLocation NETHERITE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/netherite_block.png");

    private static final Vector3f NEUTRAL_STRETCH_VEC = new Vector3f(1.0f, 0.0f, 0.0f);

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return NETHERITE;
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

        renderBlackHoleElliptical(
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

    private static void uniformSetter(
            PostPass pass,
            Matrix4f projectionMatrix,
            Vector3fc cameraRelativePos,
            Vector2f screenPos,
            float effectRadius,
            float holeRadius,
            float distFromCamera,
            float r,
            float g,
            float b,
            float a,
            float exponent,
            Vector3f stretchDirView,
            float stretchStrength
    ) {
        float fov = (float) Math.toRadians(RBHCameraInfo.getFov());

        float safeDistance = Math.max(distFromCamera, 0.001f);
        float effectFraction = effectRadius / ((float) Math.tan(fov * 0.5f) * safeDistance);

        float expScale = 1.0f / (float) (Math.exp(5.0) - 1.0);
        float effectOffset = holeRadius / effectRadius;

        effectFraction *= effectRadius * 3.0f;

        pass.getEffect().safeGetUniform("InverseProjection").set(new Matrix4f(projectionMatrix).invert());
        pass.getEffect().safeGetUniform("Projection").set(projectionMatrix);

        pass.getEffect().safeGetUniform("HoleCenter").set(
                cameraRelativePos.x(),
                cameraRelativePos.y(),
                cameraRelativePos.z()
        );

        pass.getEffect().safeGetUniform("HoleScreenCenter").set(screenPos.x, screenPos.y);
        pass.getEffect().safeGetUniform("HoleColor").set(r, g, b, a);

        pass.getEffect().safeGetUniform("HoleRadius").set(holeRadius);
        pass.getEffect().safeGetUniform("Radius").set(effectRadius);
        pass.getEffect().safeGetUniform("Exponent").set(exponent);
        pass.getEffect().safeGetUniform("EffectOffset").set(effectOffset);

        pass.getEffect().safeGetUniform("HoleRadius2").set(holeRadius * holeRadius);
        pass.getEffect().safeGetUniform("Radius2").set(effectRadius * effectRadius);
        pass.getEffect().safeGetUniform("EffectFraction").set(effectFraction);
        pass.getEffect().safeGetUniform("ExpScale").set(expScale);

        pass.getEffect().safeGetUniform("StretchDir").set(
                stretchDirView.x,
                stretchDirView.y,
                stretchDirView.z
        );

        pass.getEffect().safeGetUniform("StretchStrength").set(stretchStrength);
    }

    private static Vector3f getViewSpaceCenter(PoseStack poseStack, Matrix4f modelViewMatrix) {
        Matrix4f objectToView = new Matrix4f(modelViewMatrix)
                .mul(poseStack.last().pose());

        return objectToView.transformPosition(
                0.0f,
                0.0f,
                0.0f,
                new Vector3f()
        );
    }

    private static Vector2f getScreenSpace(Vector3fc cameraRelativePos, Matrix4f projectionMatrix) {
        Vector4f pos4 = new Vector4f(
                cameraRelativePos.x(),
                cameraRelativePos.y(),
                cameraRelativePos.z(),
                1.0f
        );

        pos4.mul(projectionMatrix);

        float ndcX = pos4.x / pos4.w;
        float ndcY = pos4.y / pos4.w;

        float normX = ndcX * 0.5f + 0.5f;
        float normY = ndcY * 0.5f + 0.5f;

        return new Vector2f(normX, normY);
    }

    public static void renderBlackHole(
            PoseStack poseStack,
            @NotNull PostEffectRegistry.HoleEffectInstance effectInstance,
            PostEffectRegistry.RenderPhase phase,
            int packedLight,
            float effectRadius,
            float holeRadius,
            boolean rainbow,
            int color,
            float effectExponent
    ) {
        renderBlackHoleElliptical(
                poseStack,
                effectInstance,
                phase,
                packedLight,
                effectRadius,
                holeRadius,
                rainbow,
                color,
                effectExponent,
                NEUTRAL_STRETCH_VEC,
                0.0f
        );
    }

    public static void renderBlackHoleElliptical(
            PoseStack poseStack,
            @NotNull PostEffectRegistry.HoleEffectInstance effectInstance,
            PostEffectRegistry.RenderPhase phase,
            int packedLight,
            float effectRadius,
            float holeRadius,
            boolean rainbow,
            int color,
            float effectExponent,
            Vector3f stretchDir,
            float stretchStrength
    ) {
        if (!stretchDir.isFinite()) {
            stretchDir = new Vector3f(1.0f, 0.0f, 0.0f);
            stretchStrength = 0.0f;
        }

        float r = (float) ((color >> 16) & 0xFF) / 255.0f;
        float g = (float) ((color >> 8) & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;

        FboGuard mainGuard = new FboGuard();
        mainGuard.save();

        PostChain chain = PostEffectRegistry.getMutablePostChainFor(RBHRenderTypes.BLACK_HOLE_POST_SHADER);

        if (chain == null || effectInstance.passes.isEmpty()) {
            mainGuard.restore();
            return;
        }

        PostPass holePostPass = effectInstance.passes.get(0);
        RenderTarget finalTarget = holePostPass.inTarget;

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        /*
         * Critical: capture the current render matrices now.
         *
         * The actual mask sphere draw happens later inside effectInstance.setRenderFunc(...).
         * At that later time, Minecraft may have changed the global RenderSystem matrices.
         * If we do not restore these captured matrices, the mask sphere will ignore camera rotation.
         */
        Matrix4f capturedModelView = new Matrix4f(RenderSystem.getModelViewMatrix());
        Matrix4f capturedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());

        Vector3f cameraRelativePos = getViewSpaceCenter(poseStack, capturedModelView);

        PoseStack rawPoseStack = new PoseStack();
        rawPoseStack.mulPose(poseStack.last().pose());

        effectInstance.renderPhase = phase;

        Matrix4f projectionForShader = minecraft.gameRenderer.getProjectionMatrix(
                RBHCameraInfo.getFov()
        );

        Vector2f screenPos = getScreenSpace(cameraRelativePos, projectionForShader);
        float distFromCamera = cameraRelativePos.length();

        /*
         * process() sorts holes before executing renderFunc, so the distance must
         * already be current when the sort happens.
         */
        effectInstance.dist = distFromCamera;

        Matrix4f objectToView = new Matrix4f(capturedModelView)
                .mul(poseStack.last().pose());

        Vector3f stretchDirView = new Vector3f(stretchDir);
        objectToView.transformDirection(stretchDirView);

        if (stretchDirView.lengthSquared() > 0.000001f) {
            stretchDirView.normalize();
        } else {
            stretchDirView.set(1.0f, 0.0f, 0.0f);
        }

        float finalStretchStrength = stretchStrength;

        effectInstance.setRenderFunc(() -> {
            int prevDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int prevReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);

            int[] viewport = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

            boolean hadScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            int[] scissor = new int[4];

            if (hadScissor) {
                GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
            }

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
            VertexSorting previousVertexSorting = RenderSystem.getVertexSorting();

            ByteBufferBuilder byteBufferBuilder = null;

            try {
                modelViewStack.pushMatrix();
                modelViewStack.identity();
                modelViewStack.mul(capturedModelView);
                RenderSystem.applyModelViewMatrix();

                RenderSystem.setProjectionMatrix(capturedProjection, VertexSorting.DISTANCE_TO_ORIGIN);

                /*
                 * Important order:
                 *
                 * 1. Bind mask target.
                 * 2. Clear COLOR ONLY, not depth.
                 * 3. Copy current world depth from main target.
                 * 4. Bind mask target again.
                 * 5. Draw sphere mask with LEQUAL depth test.
                 *
                 * This prevents stale mask pixels from previous frames while preserving
                 * proper world occlusion.
                 */
                finalTarget.bindWrite(true);

                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

                /*
                 * Copy depth for all phases, including AFTER_ARM.
                 */
                finalTarget.copyDepthFrom(mainTarget);

                finalTarget.bindWrite(false);

                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.depthMask(true);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.disableBlend();

                byteBufferBuilder = new ByteBufferBuilder(256 * 1024);

                MultiBufferSource.BufferSource localBufferSource =
                        MultiBufferSource.immediate(byteBufferBuilder);

                RenderType renderType = RBHRenderTypes.getBlackHole(NETHERITE, finalTarget);
                VertexConsumer vertexConsumer = localBufferSource.getBuffer(renderType);

                SphereMesh.render(
                        rawPoseStack,
                        vertexConsumer,
                        effectRadius,
                        10,
                        10,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        true,
                        new Vector3f(stretchDirView),
                        finalStretchStrength
                );

                SphereMesh.render(
                        rawPoseStack,
                        vertexConsumer,
                        holeRadius,
                        8,
                        8,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        true,
                        new Vector3f(stretchDirView),
                        finalStretchStrength
                );

                localBufferSource.endBatch();
            } finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }

                modelViewStack.popMatrix();
                RenderSystem.applyModelViewMatrix();

                RenderSystem.setProjectionMatrix(previousProjection, previousVertexSorting);

                /*
                 * Restore DRAW and READ independently. Binding GL_FRAMEBUFFER
                 * afterwards would overwrite both bindings again.
                 */
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);

                GL11.glViewport(
                        viewport[0],
                        viewport[1],
                        viewport[2],
                        viewport[3]
                );

                if (hadScissor) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                    GL11.glScissor(
                            scissor[0],
                            scissor[1],
                            scissor[2],
                            scissor[3]
                    );
                } else {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }

                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
        });

        if (rainbow) {
            float[] rgb = glowColor(System.currentTimeMillis(), 6.0f, 1.0f, 0.9f, 2.0f);
            r = rgb[0];
            g = rgb[1];
            b = rgb[2];
        }

        float finalR = r;
        float finalG = g;
        float finalB = b;

        effectInstance.uniformSetter = pass -> uniformSetter(
                pass,
                projectionForShader,
                cameraRelativePos,
                screenPos,
                effectRadius,
                holeRadius,
                distFromCamera,
                finalR,
                finalG,
                finalB,
                1.0f,
                effectExponent,
                stretchDirView,
                finalStretchStrength
        );

        PostEffectRegistry.renderMutableEffectForNextTick(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
        PostEffectRegistry.getMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER).updateHole(effectInstance);

        mainGuard.restore();
    }

    public static float[] glowColor(
            long nowMs,
            float cycleSeconds,
            float saturation,
            float baseBrightness,
            float pulseSeconds
    ) {
        float hue = (nowMs % (long) (cycleSeconds * 1000.0f)) / (cycleSeconds * 1000.0f);

        float brightness = baseBrightness;

        if (pulseSeconds > 0.0f) {
            double phase = (nowMs % (long) (pulseSeconds * 1000.0f)) / (pulseSeconds * 1000.0);
            float pulse = (float) (0.5 + 0.5 * Math.sin(2.0 * Math.PI * phase));
            brightness = clamp01(baseBrightness * 0.7f + pulse * baseBrightness * 0.3f);
        }

        int rgb = Color.HSBtoRGB(hue, clamp01(saturation), clamp01(brightness));
        int hex = 0xFF000000 | (rgb & 0x00FFFFFF);

        float r = (float) ((hex >> 16) & 0xFF) / 255.0f;
        float g = (float) ((hex >> 8) & 0xFF) / 255.0f;
        float b = (float) (hex & 0xFF) / 255.0f;

        return new float[]{r, g, b};
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}