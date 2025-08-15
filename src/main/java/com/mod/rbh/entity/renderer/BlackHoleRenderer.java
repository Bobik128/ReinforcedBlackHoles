package com.mod.rbh.entity.renderer;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.lang.Math;

public class BlackHoleRenderer extends EntityRenderer<BlackHole> {
    public static final ResourceLocation NETHERITE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/netherite_block.png");
    private static Logger LOGGER = LogUtils.getLogger();

    public BlackHoleRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(@NotNull BlackHole entity, float pEntityYaw, float pPartialTick, @NotNull PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        renderBlackHole(poseStack, entity.effectInstance, PostEffectRegistry.RenderPhase.AFTER_LEVEL, pPackedLight, 0.3f, 0.15f);
    }

    private static void uniformSetter(PostPass pass, Matrix4f inverseProj, Vector3fc camRel, Vector2f screenPos,
                               float radius, float holeRadius, float distFromCam, int color) {

        // Extract RGBA from int color
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Precompute constants
        float fov = (float) Math.toRadians(IGameRenderer.get().getFovPublic());
        float effectFraction = radius / ((float) Math.tan(fov * 0.5f) * distFromCam);
        float expScale = 1.0f / (float) (Math.exp(5.0) - 1.0);

        // Set uniforms
        pass.getEffect().safeGetUniform("InverseProjection").set(inverseProj);
        pass.getEffect().safeGetUniform("HoleCenter").set(camRel.x(), camRel.y(), camRel.z());
        pass.getEffect().safeGetUniform("HoleScreenCenter").set(screenPos.x, screenPos.y);
        pass.getEffect().safeGetUniform("HoleColor").set(r, g, b, a);
        pass.getEffect().safeGetUniform("HoleRadius").set(holeRadius);
        pass.getEffect().safeGetUniform("Radius").set(radius);
        pass.getEffect().safeGetUniform("HoleRadius2").set(holeRadius * holeRadius);
        pass.getEffect().safeGetUniform("Radius2").set(radius * radius);
        pass.getEffect().safeGetUniform("EffectFraction").set(effectFraction);
        pass.getEffect().safeGetUniform("ExpScale").set(expScale);
    }

    private static Vector2f getScreenSpace(Vector3fc camRelPos, Matrix4f projMatrix) {
        Vector4f pos4 = new Vector4f(
                camRelPos.x(),
                camRelPos.y(),
                camRelPos.z(),
                1.0f
        );

        pos4.mul(projMatrix);

        // If behind camera, still produce coords but they will be flipped
        float ndcX = pos4.x / pos4.w; // -1 to 1
        float ndcY = pos4.y / pos4.w; // -1 to 1

        // Normalize to 0–1
        float normX = ndcX * 0.5f + 0.5f;
        float normY = (ndcY * 0.5f + 0.5f);

        return new Vector2f(normX, normY);
    }

    public static void renderBlackHole(
            PoseStack poseStack,
            PostEffectRegistry.HoleEffectInstance effectInstance,
            PostEffectRegistry.RenderPhase phase,
            int pPackedLight,
            float effectRadius,
            float holeRadius
    ) {
        PostChain chain = PostEffectRegistry.getMutablePostChainFor(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
        if (chain == null || effectInstance.passes.isEmpty()) return;

        PostPass holePostPass = effectInstance.passes.get(0);
        RenderTarget finalTarget = holePostPass.inTarget;
        RenderTarget swapTarget = holePostPass.outTarget;

        // ===== Save GL state =====
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();

        // ===== Ensure correct target sizes (should be done on resize, not here) =====
        Window window = Minecraft.getInstance().getWindow();
        if (finalTarget.width != window.getWidth() || finalTarget.height != window.getHeight()) {
            finalTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            swapTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        }

        // ===== Prepare post-effect uniforms =====

        Vector3fc cameraRelativePos = poseStack.last().pose().getTranslation(new Vector3f());

        PoseStack rawPoseStack = new PoseStack();
        rawPoseStack.mulPoseMatrix(poseStack.last().pose());

        // ===== Apply post effect without breaking rest of pipeline =====
        effectInstance.renderPhase = phase;

        Matrix4f preBobProjection = Minecraft.getInstance().gameRenderer.getProjectionMatrix(
                IGameRenderer.get().getFovPublic()
        );

        Vector2f screenPos = getScreenSpace(cameraRelativePos, preBobProjection);
        float distFromCam = cameraRelativePos.length();
        Matrix4f inverseProj = new Matrix4f(preBobProjection).invert();

        effectInstance.setRenderFunc(() -> {
            effectInstance.dist = distFromCam;

            // ===== Render black hole sphere into main scene =====
            BufferBuilder localBB = new BufferBuilder(256 * 1024);                  // dedicated, not the global Tesselator
            MultiBufferSource.BufferSource localBuf = MultiBufferSource.immediate(localBB);

            RenderType rt = RBHRenderTypes.getBlackHole(NETHERITE, finalTarget);    // this one binds your finalTarget
            VertexConsumer vc = localBuf.getBuffer(rt);

            SphereMesh.render(rawPoseStack, vc, effectRadius, 10, 10, pPackedLight, OverlayTexture.NO_OVERLAY);

            SphereMesh.render(rawPoseStack, vc, holeRadius, 8, 8, pPackedLight, OverlayTexture.NO_OVERLAY);

            // Flush ONLY our stuff; this unbinds your RT and restores GL state for later draws
            localBuf.endBatch();
            mainTarget.bindWrite(true);
        });

        effectInstance.uniformSetter = (pass) ->
                uniformSetter(pass, inverseProj, cameraRelativePos, screenPos,
                        effectRadius, holeRadius, distFromCam, 0xFFFFFF00);

        PostEffectRegistry.renderMutableEffectForNextTick(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
        PostEffectRegistry.getMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER).updateHole(effectInstance);

        // ===== Restore framebuffer & viewport =====
        mainTarget.bindWrite(true);
    }


    @Override
    public ResourceLocation getTextureLocation(BlackHole pEntity) {
        return null;
    }
}
