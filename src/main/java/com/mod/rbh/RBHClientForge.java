package com.mod.rbh;

import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class RBHClientForge {

    public static void init(IEventBus modBus, IEventBus forgeBus) {
//        modBus.addListener(RBHClientForge::onClientSetup);

        forgeBus.addListener(RBHClientForge::onClientTick);
        forgeBus.addListener(RifleHoleEffectInstanceHolder::resetEffectCounter);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        RifleHoleEffectInstanceHolder.clientTick();
    }
}
