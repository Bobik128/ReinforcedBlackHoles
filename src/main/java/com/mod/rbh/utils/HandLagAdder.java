package com.mod.rbh.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class HandLagAdder {
    private static final float SMOOTHING = 0.2f;   // 0..1 (lower => more trailing)
    private static final float LAG_X     = 0.0030f; // yaw → X translate
    private static final float LAG_Y     = 0.0030f; // pitch → Y translate
    private static final float LAG_Z     = 0.0008f; // small parallax depth
    private static final float ROT_GAIN  = 0.25f;   // counter-rotate for "heft"
    private static final float MAX_DEG_STEP = 36.0f; // clamp sudden spikes

    private static boolean init = false;
    private static float smoothedYaw;
    private static float smoothedPitch;

    // stash per-frame deltas to reuse in Post
    private static float lastDyaw, lastDpitch;

    public static void lagHand(float pt, PoseStack pose) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Use view-rot getters so we line up with camera bob/smooth
        float yaw   = p.getViewYRot(pt);
        float pitch = p.getViewXRot(pt);

        if (!init) {
            smoothedYaw = yaw;
            smoothedPitch = pitch;
            init = true;
        }

        // Low-pass filter toward the live camera angles
        smoothedYaw   += (yaw   - smoothedYaw)   * SMOOTHING;
        smoothedPitch += (pitch - smoothedPitch) * SMOOTHING;

        // Camera "got ahead" of our smoothed value → that delta is the trail
        float dyaw   = Mth.clamp(yaw   - smoothedYaw,   -MAX_DEG_STEP, MAX_DEG_STEP);
        float dpitch = Mth.clamp(pitch - smoothedPitch, -MAX_DEG_STEP, MAX_DEG_STEP);

        lastDyaw = dyaw;
        lastDpitch = dpitch;

        // Translate slightly opposite the camera’s motion (feel free to swap signs)
        pose.translate(dyaw * LAG_X, dpitch * LAG_Y, Math.abs(dyaw) * -LAG_Z);

        // Add a tiny counter rotation for “heft”
        pose.mulPose(Axis.YP.rotationDegrees(dyaw * ROT_GAIN));
        pose.mulPose(Axis.XP.rotationDegrees( dpitch * ROT_GAIN));
    }
}
