package com.mod.rbh;

import com.mod.rbh.client.RBHCameraInfo;
import com.mod.rbh.client.RBHLevelRenderEvents;
import com.mod.rbh.entity.renderer.RendererRegistry;
import com.mod.rbh.shaders.RBInternalShaders;
import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class RBHClientNeoForge {

    public static void init(IEventBus modBus, IEventBus neoForgeBus) {
        // Register client-only game events here.
        RendererRegistry.register(modBus);
        RBInternalShaders.register(modBus);
        RBHCameraInfo.register(neoForgeBus);
        RBHLevelRenderEvents.register(neoForgeBus);
        neoForgeBus.addListener(RBHClientNeoForge::onClientTick);

        // Keep this only if resetEffectCounter has a valid event parameter
        // matching a NeoForge event type.
        neoForgeBus.addListener(RifleHoleEffectInstanceHolder::resetEffectCounter);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        RifleHoleEffectInstanceHolder.clientTick();
    }
}