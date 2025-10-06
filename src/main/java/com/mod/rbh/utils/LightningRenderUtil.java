package com.mod.rbh.utils;

import com.mod.rbh.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Camera;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

public final class LightningRenderUtil {

    private static int lightningCounter = 0;
    private static double lastRenderTime = 0.0;

    private LightningRenderUtil() {}

    public static class Params {
        public int recursionDepth = 4;        // detail of the jaggedness
        public int platesAround   = 6;        // number of quads around axis
        public float widthStart   = 0.05f;    // thickness at start
        public float widthEnd     = 0.02f;    // thickness at end
        public float displaceScale= 0.15f;    // how “wild” the offsets are
        public int r0=120,g0=180,b0=255,a0=220; // color/alpha at start
        public int r1=120,g1=220,b1=255,a1= 80; // color/alpha at end
        public long seed = 0L;                // stable per-bolt seed
        public boolean worldSpace = true;     // apply camera subtraction?
        public boolean doublePassGlow = true; // optional fat outer glow pass
        public float glowScale = 1.6f;        // outer pass width multiplier
        public float glowAlphaMul = 0.45f;    // outer pass alpha factor
    }

    /**
     * Render a lightning arc between start and end using RenderType.lightning().
     * Coordinate space:
     * - worldSpace=true  -> provide world positions; we’ll subtract camera.
     * - worldSpace=false -> provide local/model positions; no camera offset.
     */
    @OnlyIn(Dist.CLIENT)
    public static void renderLightning(PoseStack pose,
                                       MultiBufferSource buffers,
                                       Vec3 start, Vec3 end,
                                       Params p) {
        if (start == null || end == null) return;

        long tick = Minecraft.getInstance().level.getGameTime();
        float partialTick = Minecraft.getInstance().getPartialTick();
        if (lastRenderTime < tick + partialTick) {
            lastRenderTime = tick + partialTick;
            lightningCounter = 0;
        }

        lightningCounter++;

        if (lightningCounter > ClientConfig.maxLightningsRendering) return;

        pose.pushPose();

        if (p.worldSpace) {
            Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 cp = cam.getPosition();
            pose.translate(-cp.x, -cp.y, -cp.z);
        }

        // inner pass (core)
        drawBolt(pose, buffers, start, end, p, 1.0f, 1.0f);

        // optional outer glow (wider, softer alpha)
        if (p.doublePassGlow) {
            drawBolt(pose, buffers, start, end, p, p.glowScale, p.glowAlphaMul);
        }

        pose.popPose();
    }

    // ===== Implementation =====

    private static void drawBolt(PoseStack pose, MultiBufferSource buffers,
                                 Vec3 start, Vec3 end, Params p,
                                 float widthMul, float alphaMul) {

        VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
        Matrix4f m = pose.last().pose();

        // Deterministic RNG so the bolt doesn't flicker within the same tick/frame
        long seed = (p.seed != 0 ? p.seed :
                Double.doubleToLongBits(start.x * 31 + end.z * 17 + start.y * 13 + end.x * 7));
        RandomSource rand = RandomSource.create(seed);

        // Axis & orthonormal basis (right, up, axis)
        Vec3 axis = end.subtract(start).normalize();
        Vec3 upRef = Math.abs(axis.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = axis.cross(upRef).normalize();
        Vec3 up    = right.cross(axis).normalize();

        // Recurse
        drawBoltRecursive(vc, m, rand, start, end, axis, right, up, p.recursionDepth, p,
                p.widthStart * widthMul, p.widthEnd * widthMul, alphaMul);
    }

    private static void drawBoltRecursive(VertexConsumer vc, Matrix4f m, RandomSource rand,
                                          Vec3 a, Vec3 b, Vec3 axis, Vec3 right, Vec3 up,
                                          int depth, Params p,
                                          float widthA, float widthB, float alphaMul) {
        if (depth == 0) {
            drawSegmentPlates(vc, m, a, b, axis, right, up, p, widthA, widthB, alphaMul);
            return;
        }

        double len = b.distanceTo(a);
        double disp = len * p.displaceScale;

        double ox = (rand.nextDouble() - 0.5) * 2.0 * disp;
        double oy = (rand.nextDouble() - 0.5) * 2.0 * disp;

        Vec3 mid = a.add(b).scale(0.5).add(right.scale(ox)).add(up.scale(oy));
        float midW = (widthA + widthB) * 0.5f * 0.96f;

        drawBoltRecursive(vc, m, rand, a,  mid, axis, right, up, depth-1, p, widthA, midW, alphaMul);
        drawBoltRecursive(vc, m, rand, mid, b,  axis, right, up, depth-1, p, midW, widthB, alphaMul);
    }

    private static void drawSegmentPlates(VertexConsumer vc, Matrix4f m,
                                          Vec3 a, Vec3 b, Vec3 axis, Vec3 right, Vec3 up,
                                          Params p, float widthA, float widthB, float alphaMul) {

        // Lerp color end→end along the whole arc by distance factor t
        double totalLen = b.distanceTo(a);
        // (We also lerp per-plate implicitly; good enough visually.)
        for (int i = 0; i < p.platesAround; i++) {
            double phi = (Math.PI * 2.0) * i / p.platesAround;
            Vec3 w = right.scale(Math.cos(phi)).add(up.scale(Math.sin(phi))); // unit vec around axis
            Vec3 offA = w.scale(widthA);
            Vec3 offB = w.scale(widthB);

            Vec3 v0 = a.add(offA);
            Vec3 v1 = a.subtract(offA);
            Vec3 v2 = b.subtract(offB);
            Vec3 v3 = b.add(offB);

            // Color: simple A->B gradient
            int rA = p.r0, gA = p.g0, bA = p.b0, aA = clamp255(Math.round(p.a0 * alphaMul));
            int rB = p.r1, gB = p.g1, bB = p.b1, aB = clamp255(Math.round(p.a1 * alphaMul));

            // two triangles (v0,v1,v2) and (v0,v2,v3)
            put(vc, m, v0, rA, gA, bA, aA);
            put(vc, m, v1, rA, gA, bA, aA);
            put(vc, m, v2, rB, gB, bB, aB);

            put(vc, m, v0, rA, gA, bA, aA);
            put(vc, m, v2, rB, gB, bB, aB);
            put(vc, m, v3, rB, gB, bB, aB);
        }
    }

    private static void put(VertexConsumer vc, Matrix4f m, Vec3 p, int r, int g, int b, int a) {
        vc.vertex(m, (float)p.x, (float)p.y, (float)p.z).color(r, g, b, a).endVertex();
    }

    private static int clamp255(int x){ return Math.max(0, Math.min(255, x)); }
}
