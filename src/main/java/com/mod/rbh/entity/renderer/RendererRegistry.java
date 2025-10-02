package com.mod.rbh.entity.renderer;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.entity.RBHEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererRegistry {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(RBHEntityTypes.BLACK_HOLE_PROJECTILE.get(), BlackHoleRenderer::new);
        }
}
