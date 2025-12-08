package com.mod.rbh.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public class SphereMesh {
    private static final Quaternionf chcedQuat = new Quaternionf();

    /**
     * Renders a sphere/ellipsoid centered at the current Pose origin.
     * If the camera is inside the ellipsoid volume, renders a fullscreen quad instead.
     *
     * @param stretchDirView  axis in VIEW-SPACE (camera space) that defines the elongation direction
     * @param stretchStrength 0 = sphere, >0 elongates equally in +/- axis (s = 1 + stretchStrength)
     */
    public static void render(PoseStack poseStack,
                              VertexConsumer buffer,
                              float radius,
                              int latBands,
                              int longBands,
                              int light,
                              int overlay,
                              boolean renderInverted,
                              Vector3f stretchDirView,
                              float stretchStrength) {

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        // Keep the "de-rotation" behavior you had.
        pose.pose().rotate(pose.pose().getNormalizedRotation(chcedQuat).invert());

        // ModelView matrix (local -> view)
        Matrix4f mv = new Matrix4f(pose.pose());
        Matrix4f invMV = new Matrix4f(mv).invert();

        // ---------- Check if camera is inside the ellipsoid ----------
        // Object center in VIEW space is the translation part of mv.
        Vector3f centerView = new Vector3f();
        mv.getTranslation(centerView);
        float distSq = centerView.lengthSquared();

        // Approximate ellipsoid radius along stretch axis by scaling the original radius.
        float stretchFactor = Math.max(1.0f + stretchStrength, 1e-6f);
        float effectiveRadius = radius * stretchFactor;
        float effectiveRadiusSq = effectiveRadius * effectiveRadius;

        if (distSq <= effectiveRadiusSq) {
            // Camera is inside the ellipsoid -> draw a fullscreen quad and bail.
            renderFullscreenQuad(buffer, pose, invMV, light, overlay);
            poseStack.popPose();
            return;
        }

        // ---------- Normal sphere/ellipsoid rendering ----------
        // Bring the VIEW-SPACE direction into LOCAL space of this mesh.
        Vector3f uLocal = new Vector3f(stretchDirView).normalize();
        invMV.transformDirection(uLocal).normalize(); // ignore translation, just rotate/scale part

        final float s = Math.max(1.0f + stretchStrength, 1e-6f); // positive scale
        final float k = (s - 1.0f);                             // amount of push along axis

        // Helper that stretches only the component along uLocal
        // p' = p + k * dot(p, u) * u
        java.util.function.Function<Vec3, Vec3> stretchLocal = (Vec3 p) -> {
            float dot = (float) (p.x * uLocal.x + p.y * uLocal.y + p.z * uLocal.z);
            return new Vec3(
                    p.x + k * dot * uLocal.x,
                    p.y + k * dot * uLocal.y,
                    p.z + k * dot * uLocal.z
            );
        };

        for (int latNumber = 0; latNumber < latBands; latNumber++) {
            float theta1 = (float) (Math.PI * latNumber / latBands);
            float theta2 = (float) (Math.PI * (latNumber + 1) / latBands);

            for (int longNumber = 0; longNumber < longBands; longNumber++) {
                float phi1 = (float) (2 * Math.PI * longNumber / longBands);
                float phi2 = (float) (2 * Math.PI * (longNumber + 1) / longBands);

                Vec3 p1 = stretchLocal.apply(sphericalToCartesian(radius, theta1, phi1));
                Vec3 p2 = stretchLocal.apply(sphericalToCartesian(radius, theta2, phi1));
                Vec3 p3 = stretchLocal.apply(sphericalToCartesian(radius, theta2, phi2));
                Vec3 p4 = stretchLocal.apply(sphericalToCartesian(radius, theta1, phi2));

                // Quad from p1-p2-p3-p4 (VertexConsumer will multiply by mv via pose)
                vertex(buffer, pose, p1, light, overlay);
                vertex(buffer, pose, p2, light, overlay);
                vertex(buffer, pose, p3, light, overlay);
                vertex(buffer, pose, p4, light, overlay);
            }
        }

        poseStack.popPose();
    }

    private static Vec3 sphericalToCartesian(float r, float theta, float phi) {
        float sinTheta = (float) Math.sin(theta);
        return new Vec3(
                r * Math.cos(phi) * sinTheta,
                r * Math.cos(theta),
                r * Math.sin(phi) * sinTheta
        );
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 pos, int light, int overlay) {
        buffer.vertex(pose.pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(overlay)
                .uv2(light)
                // normals are irrelevant for your depth-only pass (and you’re overriding anyway)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    /**
     * Renders a huge quad locked in VIEW space so that it covers the whole screen.
     * Uses invMV to convert view-space coordinates back into local space before
     * submitting to the VertexConsumer (which then applies mv again).
     */
    private static void renderFullscreenQuad(VertexConsumer buffer,
                                             PoseStack.Pose pose,
                                             Matrix4f invMV,
                                             int light,
                                             int overlay) {

        // Z slightly in front of the camera, negative in view space.
        final float zView = -0.1f;
        // Big enough to cover FoV in all typical cases.
        final float size = 1000.0f;

        Vector3f v1View = new Vector3f(-size, -size, zView);
        Vector3f v2View = new Vector3f(-size,  size, zView);
        Vector3f v3View = new Vector3f( size,  size, zView);
        Vector3f v4View = new Vector3f( size, -size, zView);

        Vector3f v1Local = new Vector3f();
        Vector3f v2Local = new Vector3f();
        Vector3f v3Local = new Vector3f();
        Vector3f v4Local = new Vector3f();

        invMV.transformPosition(v1View, v1Local);
        invMV.transformPosition(v2View, v2Local);
        invMV.transformPosition(v3View, v3Local);
        invMV.transformPosition(v4View, v4Local);

        vertex(buffer, pose, new Vec3(v1Local.x, v1Local.y, v1Local.z), light, overlay);
        vertex(buffer, pose, new Vec3(v2Local.x, v2Local.y, v2Local.z), light, overlay);
        vertex(buffer, pose, new Vec3(v3Local.x, v3Local.y, v3Local.z), light, overlay);
        vertex(buffer, pose, new Vec3(v4Local.x, v4Local.y, v4Local.z), light, overlay);
    }
}