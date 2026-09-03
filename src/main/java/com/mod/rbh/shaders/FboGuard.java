package com.mod.rbh.shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Saves/restores framebuffer, viewport and scissor state around custom renders.
 *
 * READ and DRAW framebuffer bindings are intentionally restored separately.
 * GL_FRAMEBUFFER targets both at once, so binding it after the separate restores
 * would destroy the state it just restored.
 */
public final class FboGuard {

    private int drawFramebuffer;
    private int readFramebuffer;

    private final int[] viewport = new int[4];
    private final int[] scissor = new int[4];

    private boolean hadScissor;

    public void save() {
        drawFramebuffer = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        readFramebuffer = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        hadScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (hadScissor) {
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
        }
    }

    public void restore() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);

        GL11.glViewport(
                viewport[0],
                viewport[1],
                viewport[2],
                viewport[3]
        );

        if (hadScissor) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(
                    scissor[0],
                    scissor[1],
                    scissor[2],
                    scissor[3]
            );
        } else {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }
}