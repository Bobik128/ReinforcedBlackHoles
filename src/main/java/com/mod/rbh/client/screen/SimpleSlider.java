package com.mod.rbh.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

public class SimpleSlider extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final DoubleConsumer onValueChanged;

    /**
     * @param x Screen X
     * @param y Screen Y
     * @param width slider width
     * @param height slider height
     * @param min minimum value
     * @param max maximum value
     * @param initial initial value
     * @param onValueChanged callback when slider value changes
     */
    public SimpleSlider(int x, int y, int width, int height,
                        double min, double max, double initial,
                        DoubleConsumer onValueChanged) {

        super(x, y, width, height,
                Component.literal(String.valueOf(initial)),
                (initial - min) / (max - min)); // value must be normalized 0–1

        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;

        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double realValue = getRealValue();
        this.setMessage(Component.literal(String.format("%.2f", realValue)));
    }

    @Override
    protected void applyValue() {
        double real = getRealValue();
        onValueChanged.accept(real);
    }

    private double getRealValue() {
        return min + this.value * (max - min);
    }
}

