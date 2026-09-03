package com.mod.rbh.shaders;

import com.google.gson.JsonSyntaxException;
import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.api.IPostChain;
import com.mod.rbh.api.IPostPass;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PostEffectRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ResourceLocation> registry = new ArrayList<>();
    private static final List<ResourceLocation> mutableRegistry = new ArrayList<>();

    private static final Map<ResourceLocation, PostEffect> postEffects = new HashMap<>();
    private static final Map<ResourceLocation, MutablePostEffect> mutablePostEffects = new HashMap<>();

    private static double lastFrameTime = 0.0;

    /**
     * Called once when the Minecraft frame changes.
     */
    protected static void changeFrame() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        double nowFrame = minecraft.level.getGameTime()
                + minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        if (nowFrame != lastFrameTime) {
            lastFrameTime = nowFrame;

            for (MutablePostEffect fx : mutablePostEffects.values()) {
                fx.resetFrame();
            }
        }
    }

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
     *
     * IMPORTANT:
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

    public static void blitEffects() {
        for (PostEffect fx : postEffects.values()) {
            if (fx.postChain != null && fx.isEnabled()) {
                fx.getRenderTarget().clear(Minecraft.ON_OSX);
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
                fx.getRenderTarget().clear(Minecraft.ON_OSX);
            }
        }

        changeFrame();
    }

    public static void processEffects(
            RenderTarget mainTarget,
            float partialTick,
            RenderPhase phase
    ) {
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

        /**
         * Reused every frame to avoid creating a new list for sorting.
         */
        private final List<HoleEffectInstance> sortedHoles = new ArrayList<>();

        public MutablePostEffect(PostChain postChain, boolean enabled) {
            super(postChain, null, enabled);
        }

        @Override
        public RenderTarget getRenderTarget() {
            return null;
        }

        @Override
        public void resize(int width, int height) {
            super.resize(width, height);

            /*
             * This is only called when the actual window/framebuffer
             * size changes.
             */
            for (HoleEffectInstance hole : holes.keySet()) {
                hole.resize(width, height);
            }
        }

//        /**
//         * Adds/updates a black hole for the next few frames.
//         *
//         * IMPORTANT:
//         * This method intentionally does NOT resize the framebuffer.
//         */
//        public void updateHole(HoleEffectInstance hole) {
//            if (!holes.containsKey(hole) && holes.size() >= 80) {
//                ReinforcedBlackHoles.LOGGER.warn(
//                        "Too many black hole effects registered, skipping!"
//                );
//                return;
//            }
//
//            if (hole.passes.isEmpty()) {
//                return;
//            }
//
//            PostPass pass = hole.passes.get(0);
//
//            if (pass instanceof IPostPass pp) {
//                pp.toRunOnProcess(hole.uniformSetter);
//            } else {
//                IPostPass.fromPostPass(pass)
//                        .toRunOnProcess(hole.uniformSetter);
//            }
//
//            /*
//             * Keep the effect alive for four frames.
//             */
//            holes.put(hole, 4);
//        }

        public void process(RenderPhase phase) {
            switch (phase) {
                case AFTER_LEVEL -> ranTimeAfterLevel++;
                case AFTER_ARM -> ranTimeAfterArm++;
            }

            List<PostPass> passes =
                    IPostChain.fromPostChain(this.postChain).getPostPasses();

            /*
             * ALWAYS remove passes from the previous phase/frame first.
             *
             * If this is done after the holes.isEmpty() check, the PostChain can
             * retain stale passes and process an old black-hole effect again.
             */
            passes.clear();

            if (holes.isEmpty()) {
                return;
            }

            /*
             * Build the list once and sort it.
             *
             * Furthest first is preserved from your original implementation,
             * which is important for overlapping black holes.
             */
            sortedHoles.clear();

            for (HoleEffectInstance hole : holes.keySet()) {
                if (hole.renderPhase == phase) {
                    sortedHoles.add(hole);
                }
            }

            sortedHoles.sort(
                    Comparator.comparingDouble((HoleEffectInstance hole) -> hole.dist)
                            .reversed()
            );

            /*
             * Execute the render callback and append this hole's post pass.
             *
             * Because the passes are ordered by distance, overlapping holes
             * continue to be processed in the same order as before.
             */
            for (HoleEffectInstance hole : sortedHoles) {
                hole.render();
                passes.addAll(hole.passes);
            }

            /*
             * Decrease lifetimes and remove expired effects.
             */
            toRemove.clear();

            for (Map.Entry<HoleEffectInstance, Integer> entry : holes.entrySet()) {
                int lifetime = entry.getValue();

                if (lifetime <= 0) {
                    toRemove.add(entry.getKey());
                }
            }

            for (HoleEffectInstance hole : toRemove) {
                holes.remove(hole);
//                hole.close();
            }

            for (Map.Entry<HoleEffectInstance, Integer> entry : holes.entrySet()) {
                entry.setValue(entry.getValue() - 1);
            }
        }

        public void wipe() {
            for (HoleEffectInstance hole : holes.keySet()) {
                if (hole.passes.isEmpty()) {
                    continue;
                }

                hole.passes.get(0).inTarget.clear(Minecraft.ON_OSX);
                hole.passes.get(0).outTarget.clear(Minecraft.ON_OSX);
            }
        }

        public void updateHole(HoleEffectInstance hole) {
            if (!holes.containsKey(hole) && holes.size() >= 80) {
                ReinforcedBlackHoles.LOGGER.warn(
                        "Too many black hole effects registered, skipping!"
                );
                return;
            }

            if (hole.passes.isEmpty()) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();

            // Synchronize the hole's private framebuffers with the current window.
            // HoleEffectInstance.resize() has a dimension guard, so this does
            // nothing during normal frames.
            hole.resize(window.getWidth(), window.getHeight());

            PostPass pass = hole.passes.get(0);

            if (pass instanceof IPostPass pp) {
                pp.toRunOnProcess(hole.uniformSetter);
            } else {
                IPostPass.fromPostPass(pass)
                        .toRunOnProcess(hole.uniformSetter);
            }

            /*
             * Preserve the original lifetime tolerance. process() can run in
             * multiple render phases during one visual frame, so a lifetime of
             * two is too aggressive and can make effects disappear/flicker.
             */
            holes.put(hole, 4);
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

        /*
         * These are initialized when the targets are created.
         */
        private int screenWidth;
        private int screenHeight;

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

            /*
             * The constructor/createEffectInstance path initializes these.
             */
            if (main != null) {
                this.screenWidth = main.width;
                this.screenHeight = main.height;
            }
        }

        public void setRenderFunc(Runnable func) {
            renderFunc = func;
        }

        public int getWidth() {
            return this.screenWidth;
        }

        public int getHeight() {
            return this.screenHeight;
        }

        public void render() {
            if (renderFunc != null) {
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
         * Resize ONLY when the framebuffer dimensions actually changed.
         *
         * RenderTarget.resize() reallocates GPU framebuffer storage, so it must
         * not be called every frame. At the same time, the projection matrix must
         * be rebuilt from the NEW dimensions, not from the old target size.
         */
        public void resize(int pWidth, int pHeight) {
            if (this.passes.isEmpty()) {
                this.screenWidth = pWidth;
                this.screenHeight = pHeight;
                return;
            }

            PostPass pass = this.passes.get(0);

            boolean cachedSizeMatches =
                    this.screenWidth == pWidth && this.screenHeight == pHeight;

            boolean inputSizeMatches =
                    pass.inTarget.width == pWidth && pass.inTarget.height == pHeight;

            boolean outputSizeMatches =
                    pass.outTarget.width == pWidth && pass.outTarget.height == pHeight;

            if (cachedSizeMatches && inputSizeMatches && outputSizeMatches) {
                return;
            }

            /*
             * Expensive GPU reallocations happen only for targets whose actual
             * dimensions changed.
             */
            if (!inputSizeMatches) {
                pass.inTarget.resize(
                        pWidth,
                        pHeight,
                        Minecraft.ON_OSX
                );
            }

            if (!outputSizeMatches) {
                pass.outTarget.resize(
                        pWidth,
                        pHeight,
                        Minecraft.ON_OSX
                );
            }

            this.screenWidth = pWidth;
            this.screenHeight = pHeight;

            /*
             * Build the ortho matrix after the resize and directly from the new
             * dimensions. The old implementation read main.width/main.height
             * before those targets had been resized.
             */
            this.updateOrthoMatrix(pWidth, pHeight);

            for (PostPass postPass : this.passes) {
                postPass.setOrthoMatrix(this.shaderOrthoMatrix);
            }
        }

        /**
         * Frees the framebuffer resources owned by this hole.
         */
        public void close() {
            for (PostPass pass : passes) {
                /*
                 * Each hole owns its output/input targets.
                 *
                 * Avoid closing shared Minecraft targets here.
                 */
                if (pass.inTarget != null) {
                    pass.inTarget.destroyBuffers();
                }

                if (pass.outTarget != null) {
                    pass.outTarget.destroyBuffers();
                }
            }
        }

        public static HoleEffectInstance createEffectInstance() {

            FboGuard guard = new FboGuard();
            guard.save();

            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();

            int width = window.getWidth();
            int height = window.getHeight();

            RenderTarget finalTarget =
                    new TextureTarget(
                            width,
                            height,
                            true,
                            Minecraft.ON_OSX
                    );

            RenderTarget swapTarget =
                    new TextureTarget(
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
                 * Initialize the projection matrix immediately.
                 * This avoids a resize being required on the first frame.
                 */
                Matrix4f ortho = new Matrix4f()
                        .setOrtho(
                                0.0F,
                                (float) finalTarget.width,
                                0.0F,
                                (float) finalTarget.height,
                                0.1F,
                                1000.0F
                        );

                holePass.setOrthoMatrix(ortho);
            }

            guard.restore();

            HoleEffectInstance instance =
                    new HoleEffectInstance(
                            passes,
                            null,
                            finalTarget,
                            0.0f
                    );

            /*
             * Explicitly tell the instance that its targets already have
             * the current dimensions.
             */
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

        private static final ThreadLocal<RenderPhase> CURRENT =
                new ThreadLocal<>();

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

            return phase != null
                    ? phase
                    : RenderPhase.AFTER_LEVEL;
        }
    }

    public enum RenderPhase {
        AFTER_LEVEL,
        AFTER_ARM
    }
}