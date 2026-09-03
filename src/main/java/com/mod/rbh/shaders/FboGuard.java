package com.mod.rbh.shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Saves/restores framebuffer, viewport and scissor state without collapsing
 * separate READ/DRAW framebuffer bindings back into GL_FRAMEBUFFER.
 */
public final class FboGuard {

    private int draw;
    private int read;

    private final int[] viewport = new int[4];
    private final int[] scissor = new int[4];
    private boolean hadScissor;

    public void save() {
        draw = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        read = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        hadScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (hadScissor) {
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
        }
    }

    public void restore() {
        /*
         * Do NOT bind GL_FRAMEBUFFER after these two calls. GL_FRAMEBUFFER
         * aliases both READ and DRAW and would overwrite the precise restore.
         */
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);

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
