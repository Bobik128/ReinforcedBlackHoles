package com.mod.rbh;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue DESTROY_BLOCKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DESTROY_BLOCKS = builder
                .comment("Whether Singularity Rifle black-hole projectiles can destroy blocks when they explode")
                .define("destroy_blocks", true);

        SPEC = builder.build();
    }

    public static boolean destroyBlocks = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == SPEC) {
            destroyBlocks = DESTROY_BLOCKS.get();
        }
    }
}
