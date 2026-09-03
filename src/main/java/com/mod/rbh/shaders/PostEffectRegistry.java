package com.mod.rbh.shaders;

import com.google.gson.JsonSyntaxException;
import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.api.IPostChain;
import com.mod.rbh.api.IPostPass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class PostEffectRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ResourceLocation> registry = new ArrayList<>();
    private static final List<ResourceLocation> mutableRegistry = new ArrayList<>();

    private static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();
    private static final Map<ResourceLocation, MutablePostEffect> mutablePostEffects = new HashMap<>();

    private static double lastFrameTime = 0.0;

    protected static void changeFrame() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        double nowFrame = minecraft.level.getGameTime() + minecraft.getFrameTime();

        if (nowFrame != lastFrameTime) {
            lastFrameTime = nowFrame;

            for (MutablePostEffect fx : mutablePostEffects.values()) {
                fx.resetFrame();
            }
        }
    }

    /**
     * Closes all PostChains owned by this registry.
     *
     * Mutable effects must be closed too; otherwise resource/shader reloads
     * leak the old mutable PostChain and its GPU resources.
     */
    public static void clear() {
        for (PostEffect postEffect : postEffects.values()) {
            postEffect.close();
        }

        for (MutablePostEffect postEffect : mutablePostEffects.values()) {
            postEffect.close();
        }

        postEffects.clear();
        mutablePostEffects.clear();
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
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();

        for (ResourceLocation resourceLocation : registry) {
            PostChain postChain = null;
            RenderTarget renderTarget = null;

            try {
                postChain = new PostChain(
                        minecraft.getTextureManager(),
                        minecraft.getResourceManager(),
                        minecraft.getMainRenderTarget(),
                        resourceLocation
                );

                postChain.resize(width, height);
                renderTarget = postChain.getTempTarget("final");
            } catch (IOException e) {
                LOGGER.warn("Failed to load shader: {}", resourceLocation, e);
            } catch (JsonSyntaxException e) {
                LOGGER.warn("Failed to parse shader: {}", resourceLocation, e);
            }

            postEffects.put(
                    resourceLocation,
                    new PostEffect(postChain, renderTarget, false)
            );
        }

        for (ResourceLocation resourceLocation : mutableRegistry) {
            PostChain postChain = null;

            try {
                postChain = new PostChain(
                        minecraft.getTextureManager(),
                        minecraft.getResourceManager(),
                        minecraft.getMainRenderTarget(),
                        resourceLocation
                );

                postChain.resize(width, height);
            } catch (IOException e) {
                LOGGER.warn("Failed to load shader: {}", resourceLocation, e);
            } catch (JsonSyntaxException e) {
                LOGGER.warn("Failed to parse shader: {}", resourceLocation, e);
            }

            mutablePostEffects.put(
                    resourceLocation,
                    new MutablePostEffect(postChain, false)
            );
        }
    }

    /**
     * Called when the Minecraft framebuffer/window size actually changes.
     * Do not call this every frame.
     */
    public static void resize(int width, int height) {
        for (PostEffect postEffect : postEffects.values()) {
            postEffect.resize(width, height);
        }

        for (MutablePostEffect postEffect : mutablePostEffects.values()) {
            postEffect.resize(width, height);
        }
    }

    public static RenderTarget getRenderTargetFor(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);
        return effect == null ? null : effect.getRenderTarget();
    }

    public static MutablePostEffect getMutableEffect(ResourceLocation resourceLocation) {
        return mutablePostEffects.get(resourceLocation);
    }

    public static PostChain getPostChainFor(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);
        return effect == null ? null : effect.getPostChain();
    }

    public static PostChain getMutablePostChainFor(ResourceLocation resourceLocation) {
        MutablePostEffect effect = mutablePostEffects.get(resourceLocation);
        return effect == null ? null : effect.getPostChain();
    }

    public static void renderEffectForNextTick(ResourceLocation resourceLocation) {
        PostEffect effect = postEffects.get(resourceLocation);

        if (effect != null) {
            effect.setEnabled(true);
        }
    }

    public static void renderMutableEffectForNextTick(ResourceLocation resourceLocation) {
        MutablePostEffect effect = mutablePostEffects.get(resourceLocation);

        if (effect != null) {
            effect.setEnabled(true);
        }
    }

    /**
     * Unregisters and destroys one black-hole effect instance.
     * Called when the owning client-side entity leaves the level.
     */
    public static void releaseHole(HoleEffectInstance hole) {
        if (hole == null) {
            return;
        }

        for (MutablePostEffect effect : mutablePostEffects.values()) {
            effect.removeHole(hole);
        }

        hole.close();
    }

    public static void blitEffects() {
        for (PostEffect fx : postEffects.values()) {
            if (fx.postChain != null && fx.isEnabled()) {
                RenderTarget target = fx.getRenderTarget();
                if (target != null) {
                    target.clear(Minecraft.ON_OSX);
                }
                fx.setEnabled(false);
            }
        }

        for (MutablePostEffect fx : mutablePostEffects.values()) {
            if (fx.postChain != null && fx.isEnabled()) {
                fx.wipe();
                fx.setEnabled(false);
            }
        }
    }

    public static void clearAndBindWrite(RenderTarget mainTarget) {
        for (PostEffect fx : postEffects.values()) {
            if (fx.isEnabled() && fx.postChain != null) {
                RenderTarget target = fx.getRenderTarget();
                if (target != null) {
                    target.clear(Minecraft.ON_OSX);
                }
            }
        }

        changeFrame();
    }

    public static void processEffects(RenderTarget mainTarget, float partialTick, RenderPhase phase) {
        PhaseScope.with(phase, () -> {
            if (phase == RenderPhase.AFTER_LEVEL) {
                for (PostEffect fx : postEffects.values()) {
                    if (fx.isEnabled() && fx.postChain != null) {
                        fx.postChain.process(partialTick);
                    }
                }
            }

            for (MutablePostEffect fx : mutablePostEffects.values()) {
                if (!fx.isEnabled() || fx.postChain == null) {
                    continue;
                }

                fx.process(phase);

                if (!IPostChain.fromPostChain(fx.postChain)
                        .getPostPasses()
                        .isEmpty()) {
                    fx.postChain.process(partialTick);
                }
            }
        });
    }

    // ========================================================================
    // Mutable post effect
    // ========================================================================

    public static class MutablePostEffect extends PostEffect {

        protected final Map<HoleEffectInstance, Integer> holes = new HashMap<>();

        public int ranTimeAfterLevel = 0;
        public int ranTimeAfterArm = 0;

        private final List<HoleEffectInstance> toRemove = new ArrayList<>();
        private final List<HoleEffectInstance> sortedHoles = new ArrayList<>();

        public MutablePostEffect(PostChain postChain, boolean enabled) {
            super(postChain, null, enabled);

            /*
             * The JSON used to construct this mutable PostChain contains two
             * normal bootstrap passes. This renderer never uses them: process()
             * replaces the chain's pass list with per-hole passes.
             *
             * The old code simply passes.clear()'d those objects on the first
             * rendered frame, leaking their EffectInstances. Close them once
             * here before detaching them.
             */
            if (postChain != null) {
                List<PostPass> bootstrapPasses =
                        IPostChain.fromPostChain(postChain).getPostPasses();

                for (PostPass pass : bootstrapPasses) {
                    pass.close();
                }

                bootstrapPasses.clear();
            }
        }

        @Override
        public RenderTarget getRenderTarget() {
            return null;
        }

        @Override
        public void resize(int width, int height) {
            super.resize(width, height);

            /*
             * HoleEffectInstance.resize() is guarded, so this only reallocates
             * private targets when the dimensions genuinely changed.
             */
            for (HoleEffectInstance hole : holes.keySet()) {
                hole.resize(width, height);
            }
        }

        public void process(RenderPhase phase) {
            switch (phase) {
                case AFTER_LEVEL -> ranTimeAfterLevel++;
                case AFTER_ARM -> ranTimeAfterArm++;
            }

            if (this.postChain == null) {
                return;
            }

            List<PostPass> passes =
                    IPostChain.fromPostChain(this.postChain).getPostPasses();

            /*
             * Always clear the dynamic pass list first. Leaving an old pass in
             * the PostChain can make an expired/off-screen hole run again.
             */
            passes.clear();

            if (holes.isEmpty()) {
                return;
            }

            sortedHoles.clear();

            for (HoleEffectInstance hole : holes.keySet()) {
                if (hole.renderPhase == phase && !hole.isClosed()) {
                    sortedHoles.add(hole);
                }
            }

            sortedHoles.sort(
                    Comparator.comparingDouble((HoleEffectInstance hole) -> hole.dist)
                            .reversed()
            );

            for (HoleEffectInstance hole : sortedHoles) {
                hole.render();
                passes.addAll(hole.passes);
            }

            toRemove.clear();

            for (Map.Entry<HoleEffectInstance, Integer> entry : holes.entrySet()) {
                if (entry.getValue() <= 0 || entry.getKey().isClosed()) {
                    toRemove.add(entry.getKey());
                }
            }

            for (HoleEffectInstance hole : toRemove) {
                holes.remove(hole);
            }

            for (Map.Entry<HoleEffectInstance, Integer> entry : holes.entrySet()) {
                entry.setValue(entry.getValue() - 1);
            }
        }

        public void wipe() {
            for (HoleEffectInstance hole : holes.keySet()) {
                if (hole.isClosed() || hole.passes.isEmpty()) {
                    continue;
                }

                PostPass pass = hole.passes.get(0);

                if (pass.inTarget != null) {
                    pass.inTarget.clear(Minecraft.ON_OSX);
                }

                if (pass.outTarget != null) {
                    pass.outTarget.clear(Minecraft.ON_OSX);
                }
            }
        }

        public void updateHole(HoleEffectInstance hole) {
            if (hole == null || hole.isClosed() || hole.passes.isEmpty()) {
                return;
            }

            if (!holes.containsKey(hole) && holes.size() >= 80) {
                ReinforcedBlackHoles.LOGGER.warn(
                        "Too many black hole effects registered, skipping!"
                );
                return;
            }

            Window window = Minecraft.getInstance().getWindow();

            /*
             * Cheap during normal rendering: resize() exits immediately when
             * the current targets already match the window dimensions.
             */
            hole.resize(window.getWidth(), window.getHeight());

            PostPass pass = hole.passes.get(0);

            if (pass instanceof IPostPass pp) {
                pp.toRunOnProcess(hole.uniformSetter);
            } else {
                IPostPass.fromPostPass(pass)
                        .toRunOnProcess(hole.uniformSetter);
            }

            /*
             * Keep the original four-process lifetime. This renderer has both
             * AFTER_LEVEL and AFTER_ARM phases, so "4" is intentionally not
             * treated as four literal display frames.
             */
            holes.put(hole, 4);
        }

        public void removeHole(HoleEffectInstance hole) {
            holes.remove(hole);
            sortedHoles.remove(hole);
            toRemove.remove(hole);

            if (this.postChain != null && !hole.passes.isEmpty()) {
                IPostChain.fromPostChain(this.postChain)
                        .getPostPasses()
                        .removeAll(hole.passes);
            }
        }

        @Override
        public void close() {
            /*
             * The PostChain pass list currently contains borrowed per-hole
             * PostPass objects. PostChain.close() would close those borrowed
             * passes even though the entities still own and may reuse them
             * after a resource reload. Detach them first, then close only the
             * resources actually owned by this PostChain.
             */
            if (this.postChain != null) {
                IPostChain.fromPostChain(this.postChain)
                        .getPostPasses()
                        .clear();
            }

            holes.clear();
            sortedHoles.clear();
            toRemove.clear();

            super.close();
        }

        public void resetFrame() {
            ranTimeAfterLevel = 0;
            ranTimeAfterArm = 0;
        }
    }

    // ========================================================================
    // Hole effect instance
    // ========================================================================

    public static class HoleEffectInstance {

        public final List<PostPass> passes;
        public Consumer<PostPass> uniformSetter;
        public final RenderTarget main;
        public float dist;
        public RenderPhase renderPhase;

        private @Nullable Runnable renderFunc = () -> {};
        private Matrix4f shaderOrthoMatrix;

        private int screenWidth;
        private int screenHeight;
        private boolean closed;

        /*
         * Reused for the entire lifetime of this hole. The old 1.20.1 renderer
         * allocated a new 256 KiB BufferBuilder every rendered frame.
         */
        private final BufferBuilder renderBuffer = new BufferBuilder(256 * 1024);

        public HoleEffectInstance(
                List<PostPass> passes,
                Consumer<PostPass> uniformSetter,
                RenderTarget main,
                float dist
        ) {
            this.passes = passes;
            this.uniformSetter = uniformSetter;
            this.main = main;
            this.dist = dist;
            this.renderPhase = RenderPhase.AFTER_LEVEL;

            if (main != null) {
                this.screenWidth = main.width;
                this.screenHeight = main.height;
            }
        }

        public void setRenderFunc(Runnable func) {
            if (!closed) {
                renderFunc = func;
            }
        }

        public BufferBuilder getRenderBuffer() {
            return renderBuffer;
        }

        public boolean isClosed() {
            return closed;
        }

        public int getWidth() {
            return screenWidth;
        }

        public int getHeight() {
            return screenHeight;
        }

        public void render() {
            if (!closed && renderFunc != null) {
                renderFunc.run();
                renderFunc = null;
            }
        }

        private void updateOrthoMatrix(int width, int height) {
            this.shaderOrthoMatrix = new Matrix4f()
                    .setOrtho(
                            0.0F,
                            (float) width,
                            0.0F,
                            (float) height,
                            0.1F,
                            1000.0F
                    );
        }

        /**
         * Resize only when the framebuffer dimensions actually changed.
         */
        public void resize(int pWidth, int pHeight) {
            if (closed || passes.isEmpty()) {
                return;
            }

            PostPass pass = passes.get(0);

            boolean inputMatches = pass.inTarget == null
                    || (pass.inTarget.width == pWidth && pass.inTarget.height == pHeight);

            boolean outputMatches = pass.outTarget == null
                    || (pass.outTarget.width == pWidth && pass.outTarget.height == pHeight);

            if (this.screenWidth == pWidth
                    && this.screenHeight == pHeight
                    && inputMatches
                    && outputMatches) {
                return;
            }

            /*
             * These calls recreate GPU framebuffer/texture storage, so they
             * must never run merely because another frame was rendered.
             */
            if (pass.inTarget != null
                    && (pass.inTarget.width != pWidth || pass.inTarget.height != pHeight)) {
                pass.inTarget.resize(pWidth, pHeight, Minecraft.ON_OSX);
            }

            if (pass.outTarget != null
                    && (pass.outTarget.width != pWidth || pass.outTarget.height != pHeight)) {
                pass.outTarget.resize(pWidth, pHeight, Minecraft.ON_OSX);
            }

            this.screenWidth = pWidth;
            this.screenHeight = pHeight;

            /*
             * Build the matrix after resizing and use the requested dimensions
             * directly, avoiding a stale matrix after fullscreen/window changes.
             */
            this.updateOrthoMatrix(pWidth, pHeight);

            for (PostPass postPass : this.passes) {
                postPass.setOrthoMatrix(this.shaderOrthoMatrix);
            }
        }

        /**
         * Frees GPU targets owned by this individual black hole.
         * Safe to call more than once.
         */
        public void close() {
            if (closed) {
                return;
            }

            closed = true;
            renderFunc = null;

            if (renderBuffer.building()) {
                renderBuffer.discard();
            } else {
                renderBuffer.clear();
            }

            /*
             * A PostPass owns its EffectInstance, while this hole owns the two
             * private RenderTargets supplied to that pass. Close both layers.
             */
            Set<RenderTarget> destroyedTargets =
                    Collections.newSetFromMap(new IdentityHashMap<>());

            for (PostPass pass : passes) {
                pass.close();

                if (pass.inTarget != null && destroyedTargets.add(pass.inTarget)) {
                    pass.inTarget.destroyBuffers();
                }

                if (pass.outTarget != null && destroyedTargets.add(pass.outTarget)) {
                    pass.outTarget.destroyBuffers();
                }
            }

            passes.clear();
        }

        public static HoleEffectInstance createEffectInstance() {
            FboGuard guard = new FboGuard();
            guard.save();

            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();

            int width = window.getWidth();
            int height = window.getHeight();

            RenderTarget finalTarget = new TextureTarget(
                    width,
                    height,
                    true,
                    Minecraft.ON_OSX
            );

            RenderTarget swapTarget = new TextureTarget(
                    width,
                    height,
                    true,
                    Minecraft.ON_OSX
            );

            BlitPostPass holePass = null;

            try {
                finalTarget.setFilterMode(GL11.GL_NEAREST);
                swapTarget.setFilterMode(GL11.GL_NEAREST);

                holePass = new BlitPostPass(
                        minecraft.getResourceManager(),
                        "rbh:black_hole",
                        finalTarget,
                        swapTarget
                );
            } catch (IOException e) {
                LOGGER.warn("Failed to create black hole post pass", e);
            }

            if (holePass != null) {
                holePass.addAuxAsset(
                        "MainSampler",
                        minecraft.getMainRenderTarget()::getColorTextureId,
                        width,
                        height
                );
            }

            List<PostPass> passes = new ArrayList<>(1);

            if (holePass != null) {
                passes.add(holePass);

                /*
                 * Initialize the projection immediately. With the resize guard
                 * in place, the first normal frame should not need a fake resize
                 * just to initialize this matrix.
                 */
                Matrix4f ortho = new Matrix4f()
                        .setOrtho(
                                0.0F,
                                (float) width,
                                0.0F,
                                (float) height,
                                0.1F,
                                1000.0F
                        );

                holePass.setOrthoMatrix(ortho);
            } else {
                /*
                 * Construction failed after the FBOs were allocated. They are
                 * not referenced by a PostPass, so clean them up explicitly.
                 */
                finalTarget.destroyBuffers();
                swapTarget.destroyBuffers();
            }

            guard.restore();

            HoleEffectInstance instance = new HoleEffectInstance(
                    passes,
                    null,
                    holePass != null ? finalTarget : null,
                    0.0f
            );

            instance.screenWidth = width;
            instance.screenHeight = height;

            return instance;
        }
    }

    // ========================================================================
    // Normal post effect
    // ========================================================================

    private static class PostEffect {

        protected final PostChain postChain;
        protected final RenderTarget renderTarget;
        protected boolean enabled;

        public PostEffect(
                PostChain postChain,
                RenderTarget renderTarget,
                boolean enabled
        ) {
            this.postChain = postChain;
            this.renderTarget = renderTarget;
            this.enabled = enabled;
        }

        public PostChain getPostChain() {
            return postChain;
        }

        public RenderTarget getRenderTarget() {
            return renderTarget;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void close() {
            if (postChain != null) {
                postChain.close();
            }
        }

        public void resize(int width, int height) {
            if (postChain != null) {
                postChain.resize(width, height);
            }
        }
    }

    // ========================================================================
    // Render phase
    // ========================================================================

    public static final class PhaseScope {

        private static final ThreadLocal<RenderPhase> CURRENT = new ThreadLocal<>();

        public static void with(RenderPhase phase, Runnable runnable) {
            RenderPhase old = CURRENT.get();
            CURRENT.set(phase);

            try {
                runnable.run();
            } finally {
                CURRENT.set(old);
            }
        }

        public static RenderPhase current() {
            RenderPhase phase = CURRENT.get();
            return phase != null ? phase : RenderPhase.AFTER_LEVEL;
        }
    }

    public enum RenderPhase {
        AFTER_LEVEL,
        AFTER_ARM
    }
}
