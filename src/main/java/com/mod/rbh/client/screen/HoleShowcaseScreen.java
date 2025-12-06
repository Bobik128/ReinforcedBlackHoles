package com.mod.rbh.client.screen;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class HoleShowcaseScreen extends Screen {
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "textures/gui/base.png");
    private final HoleShowcaseBlockEntity showcaseBlockEntity;
    private static final int textColor = Color.GRAY.getRGB();
    private final int imageWidth = 256;
    private final int imageHeight = 186;
    private int leftPos;
    private int topPos;

    protected HoleShowcaseScreen(HoleShowcaseBlockEntity be) {
        super(Component.translatable("screen.title"));
        showcaseBlockEntity = be;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(BASE_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.topPos + 6, textColor, false);
    }
}
