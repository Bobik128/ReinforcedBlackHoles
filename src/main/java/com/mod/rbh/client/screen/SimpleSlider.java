package com.mod.rbh.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

public class SimpleSlider extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final DoubleConsumer onValueChanged;
    private final String name;

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
                        double min, double max, double initial, String name,
                        DoubleConsumer onValueChanged) {

        super(x, y, width, height,
                Component.literal(String.valueOf(initial)),
                (initial - min) / (max - min)); // value must be normalized 0–1

        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;
        this.name = name;

        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double realValue = getRealValue();
        this.setMessage(Component.translatable(name).append(": ").append(String.format("%.2f", realValue)));
    }


//    @Override
//    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
//        double real = getRealValue();
//        onValueChanged.accept(real);
//        return super.mouseReleased(pMouseX, pMouseY, pButton);
//    }

    @Override
    protected void applyValue() {
        double real = getRealValue();
        onValueChanged.accept(real);
    }

    private double getRealValue() {
        return min + this.value * (max - min);
    }
}

