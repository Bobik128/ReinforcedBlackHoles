package com.mod.rbh.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class SphereMesh {
    private static final Quaternionf chcedQuat = new Quaternionf();
    public static void render(PoseStack poseStack, VertexConsumer buffer, float radius, int latBands, int longBands, int light, int overlay, boolean renderInverted) {
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        if (renderInverted) {
            pose.pose().rotate(pose.pose().getNormalizedRotation(chcedQuat).invert());
        }

        for (int latNumber = 0; latNumber < latBands; latNumber++) {
            float theta1 = (float) (Math.PI * latNumber / latBands);
            float theta2 = (float) (Math.PI * (latNumber + 1) / latBands);

            for (int longNumber = 0; longNumber < longBands; longNumber++) {
                float phi1 = (float) (2 * Math.PI * longNumber / longBands);
                float phi2 = (float) (2 * Math.PI * (longNumber + 1) / longBands);

                Vec3 p1 = sphericalToCartesian(radius, theta1, phi1);
                Vec3 p2 = sphericalToCartesian(radius, theta2, phi1);
                Vec3 p3 = sphericalToCartesian(radius, theta2, phi2);
                Vec3 p4 = sphericalToCartesian(radius, theta1, phi2);

                // Quad from p1-p2-p3-p4
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
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }


}

