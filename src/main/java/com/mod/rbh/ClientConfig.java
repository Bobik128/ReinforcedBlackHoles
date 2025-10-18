package com.mod.rbh;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MAX_LIGHTNINGS = BUILDER
            .comment("Max lightning strikes to be rendered at once")
            .defineInRange("strikes", 50, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MAX_LIGHTNINGS_PER_ARROW = BUILDER
            .comment("Max lightning strikes per spectral arrow (max, they can send out, does not apply for lightnings from other arrows)")
            .defineInRange("strikes_per_arrow", 6, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue DEBUG_LIGHTNINGS = BUILDER
            .comment("Spectral arrows lightning strikes debug options")
            .define("debug_lightning", false);

    private static final ForgeConfigSpec.BooleanValue INVIS_SPEC_ARROW = BUILDER
            .comment("make spectral arrows invisible")
            .define("invis_spec_arrow", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int maxLightningsRendering;
    public static int maxLightningsPerArrow;
    public static boolean debugLightning;
    public static boolean invisSpecArrow;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ClientConfig.SPEC) {
            maxLightningsRendering = MAX_LIGHTNINGS.get();
            maxLightningsPerArrow = MAX_LIGHTNINGS_PER_ARROW.get();
            debugLightning = DEBUG_LIGHTNINGS.get();
            invisSpecArrow = INVIS_SPEC_ARROW.get();
        }
    }
}
