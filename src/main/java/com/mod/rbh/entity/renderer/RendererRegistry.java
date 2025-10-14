package com.mod.rbh.entity.renderer;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.entity.ItemEntity.SingularityRifleItemEntity;
import com.mod.rbh.entity.RBHEntityTypes;
import com.mod.rbh.entity.TestBlackHole;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererRegistry {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(RBHEntityTypes.BLACK_HOLE_PROJECTILE.get(), BlackHoleProjectileRenderer::new);
            event.registerEntityRenderer(RBHEntityTypes.TEST_BLACK_HOLE.get(), BlackHoleRenderer::new);
            event.registerEntityRenderer(RBHEntityTypes.RIFLE_ITEM.get(), ItemEntityRenderer::new);
        }
}
