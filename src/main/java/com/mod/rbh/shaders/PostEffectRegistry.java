package com.mod.rbh.shaders;

import com.google.gson.JsonSyntaxException;
import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.api.IPostChain;
import com.mod.rbh.api.IPostPass;
import com.mod.rbh.compat.ShaderCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import javax.annotation.Nullable;
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

    private static double lastFrameTime = 0.0;

    protected static void changeFrame() {
        if (Minecraft.getInstance().level == null) return;
        double nowFrame = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
        if (nowFrame != lastFrameTime) {
            lastFrameTime = nowFrame;
            for (MutablePostEffect fx : mutablePostEffects.values()) {
                fx.resetFrame();
            }
        }
    }

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
        for (PostEffect fx : postEffects.values()) {
            if (fx.postChain != null && fx.isEnabled()) {
//                fx.getRenderTarget().blitToScreen(Minecraft.getInstance().getWindow().getWidth(),
//                        Minecraft.getInstance().getWindow().getHeight(), false);
                fx.getRenderTarget().clear(Minecraft.ON_OSX);
                // REMOVE: Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                fx.setEnabled(false);
            }
        }
        for (MutablePostEffect fx : mutablePostEffects.values()) {
            if (fx.postChain != null && fx.isEnabled()) {
//                fx.blitAll();
                fx.wipe();
                // REMOVE: Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                fx.setEnabled(false);
//                fx.passedOnce = false;
            }
        }
    }

    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (PostEffect fx : postEffects.values()) {
            if (fx.isEnabled() && fx.postChain != null) {
                fx.getRenderTarget().clear(Minecraft.ON_OSX);
                // REMOVE: mainTarget.bindWrite(false);
            }
        }
        changeFrame();
    }

    public static void processEffects(RenderTarget mainTarget, float f, RenderPhase phase) {
        PhaseScope.with(phase, () -> {
            if (phase == RenderPhase.AFTER_LEVEL) {
                for (PostEffect fx : postEffects.values()) {
                    if (fx.isEnabled() && fx.postChain != null) {
                        fx.postChain.process(Minecraft.getInstance().getFrameTime());
                    }
                }
            }
            for (MutablePostEffect fx : mutablePostEffects.values()) {
                if (fx.isEnabled() && fx.postChain != null) {
                    fx.process(phase);
                    if (!IPostChain.fromPostChain(fx.postChain).getPostPasses().isEmpty())
                        fx.postChain.process(Minecraft.getInstance().getFrameTime());
                }
            }
        });
    }

    public static class MutablePostEffect extends PostEffect {
        protected final Map<HoleEffectInstance, Integer> holes = new HashMap<>();
        public int ranTimeAfterLevel = 0;
        public int ranTimeAfterArm = 0;

        public MutablePostEffect(PostChain postChain, boolean enabled) {
            super(postChain, null, enabled);
        }

        @Override
        public RenderTarget getRenderTarget() {
            return null;
        }

        @Override
        public void resize(int x, int y) {
            super.resize(x, y);
            for (HoleEffectInstance hole : holes.keySet()) {
                hole.resize(x, y);
            }
        }

        private final List<HoleEffectInstance> toRemove = new ArrayList<>();
        public void process(RenderPhase phase) {
            switch (phase) {
                case AFTER_LEVEL -> ranTimeAfterLevel++;
                case AFTER_ARM -> ranTimeAfterArm++;
            }

            Map<HoleEffectInstance, Integer> resolvedPasses = new WeakHashMap<>();
            List<PostPass> passes = IPostChain.fromPostChain(this.postChain).getPostPasses();
            passes.clear();
            AtomicInteger counter = new AtomicInteger();
            holes.keySet().stream()
                    .sorted((a, b) -> Float.compare(b.dist, a.dist)) // furthest first
                    .forEach(entry -> {
                        if (entry.renderPhase == phase) {
                            int position = counter.getAndIncrement();

                            entry.render();

                            passes.addAll(entry.passes);
                            resolvedPasses.put(entry, entry.passes.size() * position);
                        }
                    });


            for (Map.Entry<HoleEffectInstance, Integer> entry : holes.entrySet()) {
                if (entry.getValue() <= 0) {
                    toRemove.add(entry.getKey());
                }
            }
            for (HoleEffectInstance hole : toRemove) {
                holes.remove(hole);
            }
            toRemove.clear();

            holes.replaceAll((key, value) -> value - 1);
        }

        public void wipe() {
            for (HoleEffectInstance hole : holes.keySet()) {
                hole.passes.get(0).inTarget.clear(Minecraft.ON_OSX);
                hole.passes.get(0).outTarget.clear(Minecraft.ON_OSX);
            }
        }

        public void updateHole(HoleEffectInstance hole) {
            if (holes.size() > 80) {
                ReinforcedBlackHoles.LOGGER.warn("Too many black hole effects registered, skipping!");
                return;
            }
            Window window = Minecraft.getInstance().getWindow();
            hole.resize(window.getWidth(), window.getHeight());
            if (hole.passes.get(0) instanceof IPostPass pp) {
                pp.toRunOnProcess(hole.uniformSetter);
            } else {
                IPostPass.fromPostPass(hole.passes.get(0)).toRunOnProcess(hole.uniformSetter);
            }
            holes.put(hole, 4);
        }

        public void resetFrame() {
            ranTimeAfterLevel = 0;
            ranTimeAfterArm = 0;
        }
    }

    public static class HoleEffectInstance {
        public List<PostPass> passes;
        public Consumer<PostPass> uniformSetter;
        public RenderTarget main;
        public float dist;
        public RenderPhase renderPhase;
        private @Nullable Runnable renderFunc = () -> {};

        private Matrix4f shaderOrthoMatrix;
        private int screenWidth;
        private int screenHeight;

        public HoleEffectInstance(List<PostPass> passes, Consumer<PostPass> uniformSetter, RenderTarget main, float dist) {
            this.passes = passes;
            this.uniformSetter = uniformSetter;
            this.main = main;
            this.dist = dist;
            this.renderPhase = RenderPhase.AFTER_LEVEL;
        }

        public void setRenderFunc(Runnable func) {
            renderFunc = func;
        }

        public void render() {
            if (renderFunc != null) {
                renderFunc.run();
                renderFunc = null;
            }
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

        public static HoleEffectInstance createEffectInstance() {

            FboGuard guard = new FboGuard();
            guard.save();

            Window window = Minecraft.getInstance().getWindow();
            RenderTarget finalTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
            RenderTarget swapTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
            BlitPostPass holePass = null;
            try {
                finalTarget.setFilterMode(GL11.GL_NEAREST);
                swapTarget.setFilterMode(GL11.GL_NEAREST);
                holePass = new BlitPostPass(Minecraft.getInstance().getResourceManager(), "rbh:black_hole", finalTarget, swapTarget);
            } catch (IOException e) {
                LOGGER.warn(e.toString());
            }

            if (holePass != null) {
                holePass.addAuxAsset("MainSampler", Minecraft.getInstance().getMainRenderTarget()::getColorTextureId, window.getWidth(), window.getHeight());
            }
            List<PostPass> passes = new ArrayList<>();
            if (holePass != null)
                passes.add(holePass);

            guard.restore();
            return new PostEffectRegistry.HoleEffectInstance(passes, null, finalTarget, 0.0f);
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

    public static final class PhaseScope {
        private static final ThreadLocal<PostEffectRegistry.RenderPhase> CURRENT = new ThreadLocal<>();
        public static void with(PostEffectRegistry.RenderPhase phase, Runnable r) {
            PostEffectRegistry.RenderPhase old = CURRENT.get();
            CURRENT.set(phase);
            try { r.run(); } finally { CURRENT.set(old); }
        }
        public static PostEffectRegistry.RenderPhase current() {
            PostEffectRegistry.RenderPhase p = CURRENT.get();
            return p != null ? p : PostEffectRegistry.RenderPhase.AFTER_LEVEL;
        }
    }

    public enum RenderPhase {
        AFTER_LEVEL,
        AFTER_ARM
    }
}