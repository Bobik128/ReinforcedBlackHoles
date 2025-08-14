package com.mod.rbh.items.renderer;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.items.SingularityRifle;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class SingularityRifleModel extends DefaultedItemGeoModel<SingularityRifle> {
    public SingularityRifleModel() {
        super(ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "singularity_rifle"));
    }

    @Override
    public void setCustomAnimations(SingularityRifle animatable, long instanceId, AnimationState<SingularityRifle> animationState) {

    }
}
