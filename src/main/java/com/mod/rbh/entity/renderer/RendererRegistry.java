package com.mod.rbh.entity.renderer;

import com.mod.rbh.blocks.custom.entity.RBHBlockEntities;
import com.mod.rbh.client.HoleShowcaseRenderer;
import com.mod.rbh.entity.RBHEntityTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class RendererRegistry {

    public static void register(IEventBus modBus) {
        modBus.addListener(RendererRegistry::registerRenderers);
    }

    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                RBHEntityTypes.BLACK_HOLE_PROJECTILE.get(),
                BlackHoleProjectileRenderer::new
        );

        event.registerEntityRenderer(
                RBHEntityTypes.TEST_BLACK_HOLE.get(),
                BlackHoleRenderer::new
        );

        event.registerBlockEntityRenderer(
                RBHBlockEntities.HOLE_SHOWCASE_BE.get(),
                HoleShowcaseRenderer::new
        );
    }
}