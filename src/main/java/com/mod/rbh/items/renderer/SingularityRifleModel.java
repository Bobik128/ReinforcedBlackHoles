package com.mod.rbh.items.renderer;

import com.ibm.icu.impl.Pair;
import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.client.RifleShootAnimHelper;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.utils.FirearmDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

import java.util.HashMap;
import java.util.Map;

public class SingularityRifleModel extends DefaultedItemGeoModel<SingularityRifle> {
    private static final float MAX_ANGLE = 16.0f;
    public SingularityRifleRenderer renderer;
    private static final Map<Long, Pair<Double, Float>> shootTriggered = new HashMap<>(); // <item, <trigger tick, start angle>>

    public SingularityRifleModel() {
        super(ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "singularity_rifle"));
    }

    @Override
    public void setCustomAnimations(SingularityRifle animatable, long instanceId, AnimationState<SingularityRifle> animationState) {
        CoreGeoBone lowerHinge = this.getAnimationProcessor().getBone("lowerHinge");
        CoreGeoBone lowerHinge2 = this.getAnimationProcessor().getBone("lowerHinge2");
        CoreGeoBone upperHinge = this.getAnimationProcessor().getBone("upperHinge");
        CoreGeoBone upperHinge2 = this.getAnimationProcessor().getBone("upperHinge2");
        CoreGeoBone holeInjector = this.getAnimationProcessor().getBone("holeInjector");

        if (lowerHinge != null && lowerHinge2 != null && upperHinge != null && upperHinge2 != null && holeInjector != null) {
            float modifier;
            float angle;

            ItemStack stack = renderer.getCurrentItemStack();
            long stackId = GeoItem.getId(stack);

            if (RifleShootAnimHelper.rifleShooting(stack) && Minecraft.getInstance().level != null) {
                modifier = (float) RifleShootAnimHelper.getChargeLevel(stack) / SingularityRifle.MAX_CHARGE_LEVEL;
                modifier = modifier * modifier;
                angle = (float) Math.toRadians(modifier * MAX_ANGLE);

                shootTriggered.put(stackId, Pair.of((double) (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime()), angle));
                RifleShootAnimHelper.remove(stack);
            } else {
                modifier = (float) FirearmDataUtils.getChargeLevel(stack) / SingularityRifle.MAX_CHARGE_LEVEL;
                modifier = modifier * modifier;
                angle = (float) Math.toRadians(modifier * MAX_ANGLE);
            }

            if (shootTriggered.containsKey(stackId) && Minecraft.getInstance().level != null) {
                double startTick = shootTriggered.get(stackId).first;
                float startAngle = shootTriggered.get(stackId).second;

                double nowTick = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
                float customAngle = (float) (startAngle - (nowTick - startTick) * 0.001f) + 0.01f;

                if (customAngle >= angle) {
                    angle = customAngle;
                } else {
                    shootTriggered.remove(stackId);
                }
            }

            lowerHinge.updateRotation(-angle, 0, 0);
            lowerHinge2.updateRotation(angle, 0, 0);

            upperHinge2.updateRotation(-angle, 0, 0);
            upperHinge.updateRotation(angle, 0, 0);

            holeInjector.updatePosition(0, 0, modifier * 1);
        }
    }
}
