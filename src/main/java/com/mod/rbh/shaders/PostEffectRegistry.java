package com.mod.rbh.shaders;

import com.google.gson.JsonSyntaxException;
import com.mod.rbh.api.IPostChain;
import com.mod.rbh.api.IPostPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PostEffectRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ResourceLocation> registry = new ArrayList<>();
    private static final List<ResourceLocation> mutableRegistry = new ArrayList<>();

    private static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();
    private static final Map<ResourceLocation, MutablePostEffect> mutablePostEffects = new HashMap<>();

    public static void clear() {
        for (PostEffect postEffect : postEffects.values())
            postEffect.close();
        postEffects.clear();
    }

    public static void registerEffect(ResourceLocation resourceLocation) {
        registry.add(resourceLocation);
    }

    public static void registerMutableEffect(ResourceLocation resourceLocation) {
        mutableRegistry.add(resourceLocation);
    }

    public static void onInitializeOutline() {
        clear();
        Minecraft minecraft = Minecraft.getInstance();
        for (ResourceLocation resourceLocation : registry) {
            PostChain postChain;
            RenderTarget renderTarget;
            try {
                postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), resourceLocation);
                postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
                renderTarget = postChain.getTempTarget("final");
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to load shader: {}", resourceLocation, ioexception);
                postChain = null;
                renderTarget = null;
            } catch (JsonSyntaxException jsonsyntaxexception) {
                LOGGER.warn("Failed to parse shader: {}", resourceLocation, jsonsyntaxexception);
                postChain = null;
                renderTarget = null;
            }
            postEffects.put(resourceLocation, new PostEffect(postChain, renderTarget, false));

        }
        for (ResourceLocation resourceLocation : mutableRegistry) {
            PostChain postChain;
            try {
                postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), resourceLocation);
                postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to load shader: {}", resourceLocation, ioexception);
                postChain = null;
            } catch (JsonSyntaxException jsonsyntaxexception) {
                LOGGER.warn("Failed to parse shader: {}", resourceLocation, jsonsyntaxexception);
                postChain = null;
            }
            mutablePostEffects.put(resourceLocation, new MutablePostEffect(postChain, false));

        }
    }

    public static void resize(int x, int y) {
        for (PostEffect postEffect : postEffects.values())
            postEffect.resize(x, y);
        for (PostEffect postEffect : mutablePostEffects.values())
            postEffect.resize(x, y);
    }

    public static RenderTarget getRenderTargetFor(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);
        return (effect == null) ? null : effect.getRenderTarget();
    }

    public static MutablePostEffect getMutableEffect(ResourceLocation resourceLocation) {
        return mutablePostEffects.get(resourceLocation);
    }

    public static PostChain getPostChainFor(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);
        return (effect == null) ? null : effect.getPostChain();
    }

    public static PostChain getMutablePostChainFor(ResourceLocation blackHolePostShader) {
        MutablePostEffect effect = mutablePostEffects.get(blackHolePostShader);
        return (effect == null) ? null : effect.getPostChain();
    }

    public static void renderEffectForNextTick(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);
        if (effect != null)
            effect.setEnabled(true);
    }

    public static void renderMutableEffectForNextTick(ResourceLocation resourceLocation) {
        MutablePostEffect effect = mutablePostEffects.get(resourceLocation);
        if (effect != null)
            effect.setEnabled(true);
    }

    public static void blitEffects() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        for (PostEffect postEffect : postEffects.values()) {
            if (postEffect.postChain != null && postEffect.isEnabled()) {
                postEffect.getRenderTarget().blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
                postEffect.getRenderTarget().clear(Minecraft.ON_OSX);
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                postEffect.setEnabled(false);
            }
        }
        for (MutablePostEffect postEffect : mutablePostEffects.values()) {
            if (postEffect.postChain != null && postEffect.isEnabled()) {
                postEffect.blitAll();
                postEffect.wipe();
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                postEffect.setEnabled(false);
            }
        }
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (PostEffect postEffect : postEffects.values()) {
            if (postEffect.isEnabled() && postEffect.postChain != null) {
                postEffect.getRenderTarget().clear(Minecraft.ON_OSX);
                mainTarget.bindWrite(false);
            }
        }
        for (MutablePostEffect postEffect : mutablePostEffects.values()) {
            if (postEffect.isEnabled() && postEffect.postChain != null) {
                postEffect.wipe();
                mainTarget.bindWrite(false);
            }
        }
    }

    public static void processEffects(RenderTarget mainTarget, float f) {
        for (PostEffect postEffect : postEffects.values()) {
            if (postEffect.isEnabled() && postEffect.postChain != null) {
                postEffect.postChain.process(Minecraft.getInstance().getFrameTime());
                mainTarget.bindWrite(false);
            }
        }
        for (MutablePostEffect postEffect : mutablePostEffects.values()) {
            if (postEffect.isEnabled() && postEffect.postChain != null) {
                postEffect.process();
                postEffect.postChain.process(Minecraft.getInstance().getFrameTime());
                mainTarget.bindWrite(false);
            }
        }
    }


    public static class MutablePostEffect extends PostEffect {
        protected final Set<HoleEffectInstance> holes = new HashSet<>();

        public MutablePostEffect(PostChain postChain, boolean enabled) {
            super(postChain, null, enabled);
        }

        @Override
        public RenderTarget getRenderTarget() {
            return null;
        }

        public void blitAll() {
//            holes.stream()
//                    .sorted((a, b) -> Float.compare(b.dist, a.dist)) // furthest first
//                    .forEach(entry -> {
//                        entry.main.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
//                    });

//            for (HoleEffectInstance hole : holes) {
//                hole.main.blitToScreen(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false);
//            }
        }

        @Override
        public void resize(int x, int y) {
            super.resize(x, y);
            for (HoleEffectInstance hole : holes) {
                hole.resize(x, y);
            }
        }

        public void process() {
            Map<HoleEffectInstance, Integer> resolvedPasses = new HashMap<>();
            List<PostPass> passes = IPostChain.fromPostChain(this.postChain).getPostPasses();
            passes.clear();
            AtomicInteger counter = new AtomicInteger();
            holes.stream()
                    .sorted((a, b) -> Float.compare(b.dist, a.dist)) // furthest first
                    .forEach(entry -> {
                        int position = counter.getAndIncrement();

                        passes.addAll(entry.passes);
                        resolvedPasses.put(entry, entry.passes.size() * position);
                    });
        }

        public void wipe() {
            for (HoleEffectInstance hole : holes) {
                hole.passes.get(0).inTarget.clear(Minecraft.ON_OSX);
                hole.passes.get(0).outTarget.clear(Minecraft.ON_OSX);
            }
        }

        public void updateHole(HoleEffectInstance hole) {
            Window window = Minecraft.getInstance().getWindow();
            hole.resize(window.getWidth(), window.getHeight());
            if (hole.passes.get(0) instanceof IPostPass pp) {
                pp.toRunOnProcess(hole.uniformSetter);
            } else {
                IPostPass.fromPostPass(hole.passes.get(0)).toRunOnProcess(hole.uniformSetter);
            }
            holes.add(hole);
        }
    }

    public static class HoleEffectInstance {
        public List<PostPass> passes;
        public Consumer<PostPass> uniformSetter;
        public RenderTarget main;
        public float dist;

        private Matrix4f shaderOrthoMatrix;
        private int screenWidth;
        private int screenHeight;

        public HoleEffectInstance(List<PostPass> passes, Consumer<PostPass> uniformSetter, RenderTarget main, float dist) {
            this.passes = passes;
            this.uniformSetter = uniformSetter;
            this.main = main;
            this.dist = dist;
        }

        private void updateOrthoMatrix() {
            this.shaderOrthoMatrix = (new Matrix4f()).setOrtho(0.0F, (float)this.main.width, 0.0F, (float)this.main.height, 0.1F, 1000.0F);
        }

        public void resize(int pWidth, int pHeight) {
            this.screenWidth = pWidth;
            this.screenHeight = pHeight;
            this.updateOrthoMatrix();

            for(PostPass postpass : this.passes) {
                postpass.setOrthoMatrix(this.shaderOrthoMatrix);
            }

            passes.get(0).outTarget.resize(pWidth, pHeight, Minecraft.ON_OSX);
        }
    }
    private static class PostEffect {
        protected final PostChain postChain;

        protected final RenderTarget renderTarget;

        protected boolean enabled;

        public PostEffect(PostChain postChain, RenderTarget renderTarget, boolean enabled) {
            this.postChain = postChain;
            this.renderTarget = renderTarget;
            this.enabled = enabled;
        }

        public PostChain getPostChain() {
            return this.postChain;
        }

        public RenderTarget getRenderTarget() {
            return this.renderTarget;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void close() {
            if (this.postChain != null)
                this.postChain.close();
        }

        public void resize(int x, int y) {
            if (this.postChain != null)
                this.postChain.resize(x, y);
        }
    }
}