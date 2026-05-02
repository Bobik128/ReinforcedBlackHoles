package com.mod.rbh;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MAX_LIGHTNINGS = BUILDER
            .comment("Max lightning strikes to be rendered at once")
            .defineInRange("strikes", 50, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_LIGHTNINGS_PER_ARROW = BUILDER
            .comment("Max lightning strikes per spectral arrow (max, they can send out, does not apply for lightnings from other arrows)")
            .defineInRange("strikes_per_arrow", 6, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue DEBUG_LIGHTNINGS = BUILDER
            .comment("Spectral arrows lightning strikes debug options")
            .define("debug_lightning", false);

    private static final ModConfigSpec.BooleanValue INVIS_SPEC_ARROW = BUILDER
            .comment("make spectral arrows invisible")
            .define("invis_spec_arrow", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int maxLightningsRendering;
    public static int maxLightningsPerArrow;
    public static boolean debugLightning;
    public static boolean invisSpecArrow;

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        modEventBus.addListener(ClientConfig::onLoad);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        ModConfig config = event.getConfig();

        if (config.getSpec() == SPEC) {
            maxLightningsRendering = MAX_LIGHTNINGS.get();
            maxLightningsPerArrow = MAX_LIGHTNINGS_PER_ARROW.get();
            debugLightning = DEBUG_LIGHTNINGS.get();
            invisSpecArrow = INVIS_SPEC_ARROW.get();
        }
    }
}