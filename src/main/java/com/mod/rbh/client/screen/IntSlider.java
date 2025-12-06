package com.mod.rbh.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class IntSlider extends AbstractSliderButton {

    private final IntConsumer onValueChanged;
    private final int min;
    private final int max;
    private final String name;

    /**
     * @param x screen X
     * @param y screen Y
     * @param width slider width
     * @param height slider height
     * @param min minimum integer value
     * @param max maximum integer value
     * @param initial initial integer value
     * @param onValueChanged callback receives the current int value
     */
    public IntSlider(int x, int y, int width, int height,
                     int min, int max, int initial, String name,
                     IntConsumer onValueChanged) {

        super(x, y, width, height,
                Component.literal(String.valueOf(initial)),
                (double)(initial - min) / (max - min)); // normalized internally

        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;
        this.name = name;

        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable(name).append(": ").append(String.valueOf(getValue())));
    }

//    @Override
//    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
//        onValueChanged.accept(getValue());
//        return super.mouseReleased(pMouseX, pMouseY, pButton);
//    }

    @Override
    protected void applyValue() {
        onValueChanged.accept(getValue());
    }

    /** Returns the current slider value as int */
    public int getValue() {
        return min + (int) Math.round(this.value * (max - min));
    }
}