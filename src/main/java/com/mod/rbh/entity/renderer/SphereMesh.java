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

        // You were “de-rotating” the pose; keep doing it to match your old behavior.
        pose.pose().rotate(pose.pose().getNormalizedRotation(chcedQuat).invert());

        // --- Prepare stretch math in LOCAL space (pre-pose) ---
        // mv is the matrix that will transform local -> view when you call buffer.vertex(mv, ...)
        Matrix4f mv = new Matrix4f(pose.pose());
        Matrix4f invMV = new Matrix4f(mv).invert();

        // Bring the VIEW-SPACE direction into LOCAL space of this mesh.
        Vector3f uLocal = new Vector3f(stretchDirView).normalize();
        invMV.transformDirection(uLocal).normalize(); // ignore translation, just rotate/scale part

        final float s = Math.max(1.0f + stretchStrength, 1e-6f); // positive scale
        final float k = (s - 1.0f);                             // amount of push along axis

        // Helper that stretches only the component along uLocal
        // p' = p + k * dot(p, u) * u
        java.util.function.Function<Vec3, Vec3> stretchLocal = (Vec3 p) -> {
            float dot = (float)(p.x * uLocal.x + p.y * uLocal.y + p.z * uLocal.z);
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

                // Quad from p1-p2-p3-p4 (VertexConsumer will multiply by mv)
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
}