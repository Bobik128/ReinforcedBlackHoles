package com.mod.rbh.items.renderer;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.utils.FirearmDataUtils;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class SingularityRifleModel extends DefaultedItemGeoModel<SingularityRifle> {
    private static final float MAX_ANGLE = 16.0f;
    public SingularityRifleRenderer renderer;

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
            float modifier = (float) FirearmDataUtils.getChargeLevel(renderer.getCurrentItemStack()) / SingularityRifle.MAX_CHARGE_LEVEL;
            modifier = modifier * modifier;
            float angle = (float) Math.toRadians(modifier * MAX_ANGLE);

            lowerHinge.updateRotation(-angle, 0, 0);
            lowerHinge2.updateRotation(angle, 0, 0);

            upperHinge2.updateRotation(-angle, 0, 0);
            upperHinge.updateRotation(angle, 0, 0);

            holeInjector.updatePosition(0, 0, modifier * 1);
        }
    }
}
