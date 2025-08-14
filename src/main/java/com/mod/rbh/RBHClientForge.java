package com.mod.rbh;

import com.mod.rbh.shaders.RifleHoleEffectInstanceHolder;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class RBHClientForge {

    public static void init(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(RBHClientForge::onClientSetup);
        modBus.addListener(RBHClientForge::onRegisterKeyMappings);

        forgeBus.addListener(RBHClientForge::onClientTick);
        forgeBus.addListener(RBHClientForge::onMouseInput);
        forgeBus.addListener(RBHClientForge::onKeyInput);
        forgeBus.addListener(RBHClientForge::onComputeFov);
        forgeBus.addListener(RBHClientForge::onRenderGuiOverlay);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(RBHClient::onClientSetup);
    }


    private static void onMouseInput(final InputEvent.MouseButton.Pre inputEvent) {
        RBHClient.onMouseInput(inputEvent.getButton(), inputEvent.getButton(), inputEvent.getModifiers());
    }

    private static void onKeyInput(final InputEvent.Key event) {
        RBHClient.onKeyInput(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
    }

    private static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        RBHClient.registerKeyMappings(event::register);
    }

    private static void onComputeFov(final ComputeFovModifierEvent event) {
        event.setNewFovModifier(RBHClient.modifyFov(event.getNewFovModifier(), event.getPlayer()));
    }

    private static void onRenderGuiOverlay(final RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().overlay() == VanillaGuiOverlay.HOTBAR.type().overlay()) {
        }
        // TODO crosshair
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        RifleHoleEffectInstanceHolder.clientTick();
    }
}
