package com.mod.rbh.shaders;

import com.mod.rbh.api.IPostPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.function.Consumer;

public class BlitPostPass extends PostPass implements IPostPass {
    private Consumer<PostPass> reinforcedBreakable$toRun = pass -> {};

    public BlitPostPass(
            ResourceProvider resourceProvider,
            String name,
            RenderTarget inTarget,
            RenderTarget outTarget
    ) throws IOException {
        super(resourceProvider, name, inTarget, outTarget, false);
    }

    public BlitPostPass(
            ResourceProvider resourceProvider,
            String name,
            RenderTarget inTarget,
            RenderTarget outTarget,
            boolean useLinearFilter
    ) throws IOException {
        super(resourceProvider, name, inTarget, outTarget, useLinearFilter);
    }

    @Override
    public void process(float partialTicks) {
        reinforcedBreakable$toRun.accept(this);

        super.process(partialTicks);

        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getMainRenderTarget().bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        this.outTarget.blitToScreen(
                minecraft.getWindow().getWidth(),
                minecraft.getWindow().getHeight(),
                false
        );

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        minecraft.getMainRenderTarget().bindWrite(false);
    }

    @Override
    public void toRunOnProcess(Consumer<PostPass> toRun) {
        this.reinforcedBreakable$toRun = toRun != null ? toRun : pass -> {};
    }
}