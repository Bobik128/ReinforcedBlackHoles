package com.mod.rbh.mixin.client;

import com.ibm.icu.impl.Pair;
import com.mod.rbh.ClientConfig;
import com.mod.rbh.utils.LightningRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ArrowRenderer.class)
public class ArrowRendererMixin<T extends AbstractArrow> {
    @Unique
    private static Set<Pair<Integer, Integer>> reinforcedBlackHoles$usedArrows = new HashSet<>();
    @Unique
    private static long reinforcedBlackHoles$lastTick = 0;

    @Inject(method = "render(Lnet/minecraft/world/entity/projectile/AbstractArrow;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void renderLightning(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (ClientConfig.debugLightning && pEntity instanceof SpectralArrow) {

            long tick = Minecraft.getInstance().level.getGameTime();
            if (reinforcedBlackHoles$lastTick < tick) {
                reinforcedBlackHoles$lastTick = tick;
                reinforcedBlackHoles$usedArrows.clear();
            }

            int size = 30;
            List<SpectralArrow> otherArrows = pEntity.level().getEntitiesOfClass(SpectralArrow.class, AABB.ofSize(pEntity.position(), size, size, size));
            int i = 0;
            for (SpectralArrow arrow : otherArrows) {
                if (i >= ClientConfig.maxLightningsPerArrow) return;

                if (arrow.equals(pEntity)) continue;

                boolean used = false;
                for (Pair pair : reinforcedBlackHoles$usedArrows) {
                    if ( (int) pair.first == arrow.getId() && (int) pair.second == pEntity.getId()) {
                        used = true;
                        break;
                    }
                }
                if (used) continue;

                var p = new LightningRenderUtil.Params();
                p.worldSpace = false;
                p.seed = (System.nanoTime() >> 16);

                p.widthStart = 0.06f;
                p.widthEnd = 0.03f;
                p.displaceScale = 0.15f;

                LightningRenderUtil.renderLightning(pMatrixStack, pBuffer, Vec3.ZERO, arrow.position().subtract(pEntity.position()), p);

                reinforcedBlackHoles$usedArrows.add(Pair.of(pEntity.getId(), arrow.getId()));

                i++;
            }
            if (ClientConfig.invisSpecArrow) ci.cancel();
        }
    }
}
