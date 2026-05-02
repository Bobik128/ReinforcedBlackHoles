package com.mod.rbh.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class RBHCameraInfo {
    private static float cachedFov = 70.0f;

    private RBHCameraInfo() {
    }

    public static void register(IEventBus neoForgeBus) {
        neoForgeBus.addListener(RBHCameraInfo::onComputeFov);
    }

    private static void onComputeFov(final ViewportEvent.ComputeFov event) {
        cachedFov = (float) event.getFOV();
    }

    public static float getFov() {
        return cachedFov;
    }
}