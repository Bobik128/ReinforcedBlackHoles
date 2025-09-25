package com.mod.rbh.entity.renderer;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.shaders.FboGuard;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
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

import java.awt.*;
import java.lang.Math;

public class BlackHoleRenderer extends EntityRenderer<BlackHole> {
    public static final ResourceLocation NETHERITE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/netherite_block.png");
    private static Logger LOGGER = LogUtils.getLogger();

    public BlackHoleRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(@NotNull BlackHole entity, float pEntityYaw, float pPartialTick, @NotNull PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        if (entity.effectInstance == null) return;
        renderBlackHole(poseStack, entity.effectInstance, PostEffectRegistry.RenderPhase.AFTER_LEVEL, pPackedLight, entity.getEffectSize(), entity.getSize(), entity.shouldBeRainbow());
    }

    private static void uniformSetter(PostPass pass, Matrix4f normalProj, Vector3fc camRel, Vector2f screenPos,
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
        pass.getEffect().safeGetUniform("InverseProjection").set(new Matrix4f(normalProj).invert());
//        pass.getEffect().safeGetUniform("ProjMatrix").set(normalProj);
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
            @NotNull PostEffectRegistry.HoleEffectInstance effectInstance,
            PostEffectRegistry.RenderPhase phase,
            int pPackedLight,
            float effectRadius,
            float holeRadius,
            boolean rainbow
    ) {
        FboGuard mainGuard = new FboGuard();
        mainGuard.save();

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
        effectInstance.setRenderFunc(() -> {
            effectInstance.dist = distFromCam;

            // --- Save current state ---
            int prevDrawFbo = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int prevReadFbo = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int prevFbo     = GL30.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING); // safety for packs using GL_FRAMEBUFFER
            int[] vp = new int[4]; GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
            boolean hadScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            int[] sc = new int[4]; if (hadScissor) GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, sc);

            // --- Draw spheres into your offscreen RT via your RenderType ---
            BufferBuilder bb = new BufferBuilder(256 * 1024);
            MultiBufferSource.BufferSource local = MultiBufferSource.immediate(bb);

            RenderType rt = RBHRenderTypes.getBlackHole(NETHERITE, finalTarget); // binds finalTarget
            VertexConsumer vc = local.getBuffer(rt);
            SphereMesh.render(rawPoseStack, vc, effectRadius, 10, 10, pPackedLight, OverlayTexture.NO_OVERLAY, true);
            SphereMesh.render(rawPoseStack, vc, holeRadius,   8,  8, pPackedLight, OverlayTexture.NO_OVERLAY, true);
            local.endBatch();

            // --- Restore framebuffer and raster state exactly as found ---
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo); // belt & suspenders

            GL11.glViewport(vp[0], vp[1], vp[2], vp[3]);
            if (hadScissor) { GL11.glEnable(GL11.GL_SCISSOR_TEST); GL11.glScissor(sc[0], sc[1], sc[2], sc[3]); }
            else GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Re-assert vanilla-ish defaults expected by Iris hand pass
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        });

        effectInstance.uniformSetter = (pass) ->
                uniformSetter(pass, preBobProjection, cameraRelativePos, screenPos,
                        effectRadius, holeRadius, distFromCam, rainbow ? glowColor(System.currentTimeMillis(), 6.0f, 1.0f, 0.9f, 2.0f) : 0xFFFFFF00);

        PostEffectRegistry.renderMutableEffectForNextTick(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
        PostEffectRegistry.getMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER).updateHole(effectInstance);

        mainTarget.bindWrite(true);
        mainGuard.restore();
    }


    @Override
    public ResourceLocation getTextureLocation(BlackHole pEntity) {
        return null;
    }

    public static int glowColor(long nowMs, float cycleSeconds, float sat, float baseBright, float pulseSeconds) {
        float tCycle = (nowMs % (long)(cycleSeconds * 1000f)) / (cycleSeconds * 1000f);
        float hue = tCycle; // 0..1 wraps every cycleSeconds

        float bright = baseBright;
        if (pulseSeconds > 0f) {
            double phase = (nowMs % (long)(pulseSeconds * 1000f)) / (pulseSeconds * 1000.0);
            // pulse in [ -1, 1 ] -> remap to [0,1], then scale
            float pulse = (float)(0.5 + 0.5 * Math.sin(2.0 * Math.PI * phase));
            // allow brightness to breathe around baseBright
            bright = clamp01(baseBright * 0.7f + pulse * baseBright * 0.3f);
        }

        int rgb = Color.HSBtoRGB(hue, clamp01(sat), clamp01(bright));
        // force full alpha
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
