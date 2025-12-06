package com.mod.rbh.client.screen;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.network.packet.ServerBoundUpdateHoleShowcasePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class HoleShowcaseScreen extends Screen {
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "textures/gui/base.png");
    private final HoleShowcaseBlockEntity showcaseBlockEntity;
    private static final int textColor = Color.GRAY.getRGB();
    private final int imageWidth = 256;
    private final int imageHeight = 186;
    private int leftPos;
    private int topPos;

    private HoleShowcaseBlockEntity.HoleShowcaseConfig config;

    // LAYOUT
    private static final int SLIDER_HEIGHT = 20;
    private static final int ROWS = 4;
    private static final int COLS = 2;

    private static final int VERTICAL_SPACE = 10;     // px
    private static final int START_VERTICAL_SPACE = 22;     // px
    private static final double SIDE_MARGIN = 0.05;   // % of image width
    private static final double BETWEEN_SLIDERS = 0.06; // % of image width

    protected HoleShowcaseScreen(HoleShowcaseBlockEntity be, HoleShowcaseBlockEntity.HoleShowcaseConfig config) {
        super(Component.translatable("screen.title"));
        showcaseBlockEntity = be;
        this.config = config;
    }

    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos  = (this.height - this.imageHeight) / 2;

        // Convert percentages into pixels
        int leftMargin = (int)(this.imageWidth * SIDE_MARGIN);
        int midSpace   = (int)(this.imageWidth * BETWEEN_SLIDERS);

        // Compute slider width
        int sliderWidth = (this.imageWidth - leftMargin * 2 - midSpace) / 2;

        // Starting Y position (top of the image + vertical space)
        int startY = this.topPos + START_VERTICAL_SPACE;

        int index = 0;

        for (int row = 0; row < ROWS; row++) {
            int y = startY + row * (SLIDER_HEIGHT + VERTICAL_SPACE);

            for (int col = 0; col < COLS; col++) {
                int sliderId = index++;
                int x = this.leftPos + leftMargin + col * (sliderWidth + midSpace);

                SliderNames slider;
                switch (sliderId) {
                    case 0 -> slider = SliderNames.SIZE;
                    case 1 -> slider = SliderNames.EFFECT_SIZE;
                    case 2 -> slider = SliderNames.COLOR_R;
                    case 3 -> slider = SliderNames.EFFECT_EXPONENT;
                    case 4 -> slider = SliderNames.COLOR_G;
                    case 5 -> slider = SliderNames.STRETCH_STRENGTH;
                    case 6 -> slider = SliderNames.COLOR_B;
                    default -> slider = SliderNames.HEIGHT;
                }

                if (slider.isFloat) {
                    // Float slider
                    this.addRenderableWidget(new SimpleSlider(
                            x,
                            y,
                            sliderWidth,
                            SLIDER_HEIGHT,
                            slider.min,
                            slider.max,
                            ((Number) SliderNames.getFromConfig(config, slider)).doubleValue(),
                            "rbh.slider." + slider.name,
                            (val) -> {
                                System.out.println("Slider " + slider.name + " = " + val);
                                SliderNames.setToConfig(config, slider, val);
                                RBHNetwork.sendToServer(new ServerBoundUpdateHoleShowcasePacket(showcaseBlockEntity.getBlockPos(), config, ClientScreenHandler.getClientLevel().dimension()));
                            }
                    ));
                } else {
                    // Int slider (RGB)
                    this.addRenderableWidget(new IntSlider(
                            x,
                            y,
                            sliderWidth,
                            SLIDER_HEIGHT,
                            (int) slider.min,
                            (int) slider.max,
                            (int) SliderNames.getFromConfig(config, slider),
                            "rbh.slider." + slider.name,
                            (val) -> {
                                System.out.println("Slider " + slider.name + " = " + val);
                                SliderNames.setToConfig(config, slider, val);
                                RBHNetwork.sendToServer(new ServerBoundUpdateHoleShowcasePacket(showcaseBlockEntity.getBlockPos(), config, ClientScreenHandler.getClientLevel().dimension()));
                            }
                    ));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {this.renderBackground(guiGraphics);

        // enable transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(BASE_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.topPos + 6, textColor, false);
    }

    private enum SliderNames {
        SIZE("size", true, 0.0f, 2.0f),
        EFFECT_SIZE("effect_size", true, 0.2f, 4.0f),
        EFFECT_EXPONENT("exponent", true, 1.0f, 8.0f),
        COLOR_R("color_r", false, 0, 255),
        COLOR_G("color_g", false, 0, 255),
        COLOR_B("color_b", false, 0, 255),
        STRETCH_STRENGTH("stretch", true, 0.0f, 5.0f),
        HEIGHT("height", true, 0.0f, 2.0f);

        public final String name;
        public final boolean isFloat;
        public final float min;
        public final float max;

        SliderNames(String name, boolean isFloat, float min, float max) {
            this.name = name;
            this.isFloat = isFloat;
            this.min = min;
            this.max = max;
        }

        public static Object getFromConfig(HoleShowcaseBlockEntity.HoleShowcaseConfig config, SliderNames name) {
            switch (name) {
                case SIZE -> { return config.holeRadius; }
                case EFFECT_SIZE -> { return config.effectRadius; }
                case EFFECT_EXPONENT -> { return config.effectExponent; }
                case COLOR_R -> { return (config.color >> 16) & 0xFF; } // int 0..255
                case COLOR_G -> { return (config.color >> 8) & 0xFF; }  // int 0..255
                case COLOR_B -> { return config.color & 0xFF; }         // int 0..255
                case STRETCH_STRENGTH -> { return config.stretchStrength; }
                case HEIGHT -> { return config.height; }
            }
            return 0;
        }
        public static void setToConfig(HoleShowcaseBlockEntity.HoleShowcaseConfig config, SliderNames name, Object val) {
            int red   = (config.color >> 16) & 0xFF;
            int green = (config.color >> 8)  & 0xFF;
            int blue  = config.color & 0xFF;

            switch (name) {
                case SIZE -> {config.holeRadius = ((Number) val).floatValue(); }
                case EFFECT_SIZE -> { config.effectRadius = ((Number) val).floatValue(); }
                case EFFECT_EXPONENT -> {config.effectExponent = ((Number) val).floatValue(); }
                case COLOR_R -> { config.color = ((int) val << 16) | (green << 8) | blue; } // int 0..255
                case COLOR_G -> { config.color = (red << 16) | ((int) val << 8) | blue; }  // int 0..255
                case COLOR_B -> { config.color = (red << 16) | (green << 8) | (int) val; }  // int 0..255
                case STRETCH_STRENGTH -> { config.stretchStrength = ((Number) val).floatValue(); }
                case HEIGHT -> { config.height = ((Number) val).floatValue(); }
            }
        }
    }
}
