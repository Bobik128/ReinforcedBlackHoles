package com.mod.rbh.entity.renderer;

import com.mod.rbh.api.IGameRenderer;
import com.mod.rbh.entity.BlackHole;
import com.mod.rbh.shaders.BlitPostPass;
import com.mod.rbh.shaders.PostEffectRegistry;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
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
        renderBlackHole(poseStack, entity.effectInstance, entity.position(), buffer, pPackedLight);
    }

    private static void uniformSetter(PostPass pass, Matrix4f inverseProj, Vector3f camRel, Vector2f screenPos,
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
        pass.getEffect().safeGetUniform("HoleCenter").set(camRel.x, -camRel.y, camRel.z);
        pass.getEffect().safeGetUniform("HoleScreenCenter").set(screenPos.x, screenPos.y);
        pass.getEffect().safeGetUniform("HoleColor").set(r, g, b, a);
        pass.getEffect().safeGetUniform("HoleRadius").set(holeRadius);
        pass.getEffect().safeGetUniform("Radius").set(radius);
        pass.getEffect().safeGetUniform("HoleRadius2").set(holeRadius * holeRadius);
        pass.getEffect().safeGetUniform("Radius2").set(radius * radius);
        pass.getEffect().safeGetUniform("EffectFraction").set(effectFraction);
        pass.getEffect().safeGetUniform("ExpScale").set(expScale);
    }

    public static Vector2f getScreenSpace(Vec3 worldPos, Camera camera) {
        Matrix4f viewMatrixLocal = new Matrix4f();
        Matrix4f projMatrixLocal = new Matrix4f(RenderSystem.getProjectionMatrix());

        new Quaternionf(camera.rotation()).invert().get(viewMatrixLocal);

        return worldToScreenNorm(worldPos, camera, viewMatrixLocal, projMatrixLocal);
    }

    private static Vector2f worldToScreenNorm(Vec3 worldPos, Camera camera, Matrix4f viewMatrix, Matrix4f projMatrix) {
        Vector4f pos4 = new Vector4f(
                (float)(worldPos.x - camera.getPosition().x),
                (float)(worldPos.y - camera.getPosition().y),
                (float)(worldPos.z - camera.getPosition().z),
                1.0f
        );

        pos4.mul(viewMatrix);
        pos4.mul(projMatrix);

        // If behind camera, still produce coords but they will be flipped
        float ndcX = pos4.x / pos4.w; // -1 to 1
        float ndcY = pos4.y / pos4.w; // -1 to 1

        // Normalize to 0–1
        float normX = ndcX * 0.5f + 0.5f;
        float normY = 1.0f - (ndcY * 0.5f + 0.5f);

        return new Vector2f(normX, normY);
    }

    public static void renderBlackHole(PoseStack poseStack, PostEffectRegistry.HoleEffectInstance effectInstance, Vec3 worldPosition, MultiBufferSource buffer, int pPackedLight) {
        PostChain chain = PostEffectRegistry.getMutablePostChainFor(RBHRenderTypes.BLACK_HOLE_POST_SHADER);

        PostPass holePostPass = effectInstance.passes.get(0);
        RenderTarget finalTarget = holePostPass.inTarget;
        RenderTarget swapTarget = holePostPass.outTarget;

        if (chain == null) return;

        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        float radius = 1.8f;
        float holeRadius = 0.45f;
        int longBands = 16;
        int latBands = 16;
        int color = 0xFFFFFF00;

        PostEffectRegistry.renderMutableEffectForNextTick(RBHRenderTypes.BLACK_HOLE_POST_SHADER);

        Window window = Minecraft.getInstance().getWindow();
        if (window.getHeight() != finalTarget.height || window.getWidth() != finalTarget.width) {
            finalTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            swapTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        }

        swapTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

        Camera camera = mc.gameRenderer.getMainCamera();
        Vector2f screenPos = getScreenSpace(worldPosition, camera);
        float distFromCam = mc.gameRenderer.getMainCamera().getPosition().toVector3f().distance(worldPosition.toVector3f());

        Matrix4f projection = RenderSystem.getProjectionMatrix();
        Matrix4f inverseProj = new Matrix4f(projection).invert();

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotation(new Quaternionf(camera.rotation()).invert());
        viewMatrix.translate(
                (float) -camera.getPosition().x,
                (float) -camera.getPosition().y,
                (float) -camera.getPosition().z
        );

        Vector3f camRel = new Vector3f((float) worldPosition.x, (float) worldPosition.y, (float) worldPosition.z);
        camRel.mulPosition(viewMatrix);

        effectInstance.uniformSetter = (pass) -> uniformSetter(pass, inverseProj, camRel, screenPos, radius, holeRadius, distFromCam, color);

        effectInstance.dist = (float) mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(worldPosition);
        VertexConsumer consumer = buffer.getBuffer(RBHRenderTypes.getBlackHole(NETHERITE, finalTarget));
        SphereMesh.render(poseStack, consumer, radius, latBands, longBands, pPackedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        PostEffectRegistry.getMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER).updateHole(effectInstance);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHole pEntity) {
        return null;
    }
}
