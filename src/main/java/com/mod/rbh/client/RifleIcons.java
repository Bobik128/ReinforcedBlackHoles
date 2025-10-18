package com.mod.rbh.client;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.shaders.RBHRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

public class RifleIcons {
    private static final int ATLAS_SIZE = 48;
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "textures/gui/rifle_icons.png");

    public static void drawColoredIcon(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay, Icons icon) {
        int color = switch (icon) {
            case FULL, THREE_QUARTERS, HALF -> Color.WHITE.getRGB();
            case QUARTER -> 0xFF8C00;
            case EMPTY, WARNING -> Color.RED.getRGB();
        };
        drawIcon(poseStack, bufferSource, packedLight, overlay, icon, color);
    }

    public static void drawIcon(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay, Icons icon, int color) {
        int x1 = 0;
        int x2 = 0;
        switch (icon) {
            case FULL, QUARTER -> {x1 = 0; x2 = 16;}
            case THREE_QUARTERS, EMPTY -> {x1 = 16; x2 = 32;}
            case HALF, WARNING -> {x1 = 32; x2 = 48;}
        }

        int y1 = 0;
        int y2 = 0;
        switch (icon) {
            case FULL, THREE_QUARTERS, HALF -> {y1 = 0; y2 = 16;}
            case EMPTY, QUARTER, WARNING -> {y1 = 16; y2 = 32;}
        }

        float u0 = (float) x1 / ATLAS_SIZE, u1 = (float) x2 / ATLAS_SIZE;
        float v0 = (float) y1 / ATLAS_SIZE, v1 = (float) y2 / ATLAS_SIZE;

        int xa = -8, ya = -8, xb = 8, yb = 8;

        Matrix4f mat = poseStack.last().pose();
        VertexConsumer vc = bufferSource.getBuffer(RBHRenderTypes.text(ICONS));

        Vector3f n = new Vector3f(0f, 0f, 1f);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8)  & 0xFF;
        int b = (color)       & 0xFF;
        int a = 255;

        vc.vertex(mat, xa, yb, 0).color(r, g, b, a).uv(u0, v1).overlayCoords(overlay).uv2(packedLight).normal(n.x, n.y, n.z).endVertex();
        vc.vertex(mat, xb, yb, 0).color(r, g, b, a).uv(u1, v1).overlayCoords(overlay).uv2(packedLight).normal(n.x, n.y, n.z).endVertex();
        vc.vertex(mat, xb, ya, 0).color(r, g, b, a).uv(u1, v0).overlayCoords(overlay).uv2(packedLight).normal(n.x, n.y, n.z).endVertex();
        vc.vertex(mat, xa, ya, 0).color(r, g, b, a).uv(u0, v0).overlayCoords(overlay).uv2(packedLight).normal(n.x, n.y, n.z).endVertex();
    }

    public enum Icons {
        FULL,
        THREE_QUARTERS,
        HALF,
        QUARTER,
        EMPTY,
        WARNING
    }
}
