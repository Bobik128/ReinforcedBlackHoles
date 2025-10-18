package com.mod.rbh.utils.math;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.sound.midi.MidiChannel;

public class VecFromPoseStack {
    public static Vector3f compute(PoseStack stack) {
        Matrix4f matrix = stack.last().pose();
        Vector4f origin = new Vector4f(0, 0, 0,1);

        origin.mul(matrix);
        return new Vector3f(origin.x, origin.y, origin.z);
    }
}
