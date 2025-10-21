package com.mod.rbh.shaders;

import com.mod.rbh.api.IPostPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

public class BlitPostPass extends PostPass implements IPostPass {
    private Consumer<PostPass> reinforcedBreakable$toRun = (i) -> {};

    public BlitPostPass(ResourceManager pResourceManager, String pName, RenderTarget pInTarget, RenderTarget pOutTarget) throws IOException {
        super(pResourceManager, pName, pInTarget, pOutTarget);
    }

    @Override
    public void process(float pPartialTicks) {
        reinforcedBreakable$toRun.accept(this);
        super.process(pPartialTicks);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
//        RenderSystem.enableBlend();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        outTarget.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
//        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void toRunOnProcess(Consumer<PostPass> toRun) {
        this.reinforcedBreakable$toRun = toRun;
    }
}
