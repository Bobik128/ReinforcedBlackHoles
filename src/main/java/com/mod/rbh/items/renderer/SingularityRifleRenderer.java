package com.mod.rbh.items.renderer;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.items.SingularityRifle;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SingularityRifleRenderer extends GeoItemRenderer<SingularityRifle> {
    public SingularityRifleRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "singularity_rifle")));
    }
}
