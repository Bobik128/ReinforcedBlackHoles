package com.mod.rbh.client;

import com.mod.rbh.shaders.PostEffectRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class RBHLevelRenderEvents {
    private RBHLevelRenderEvents() {
    }

    public static void register(IEventBus neoForgeBus) {
        neoForgeBus.addListener(RBHLevelRenderEvents::onRenderLevelStage);
    }

    private static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            PostEffectRegistry.blitEffects();
        }
    }
}