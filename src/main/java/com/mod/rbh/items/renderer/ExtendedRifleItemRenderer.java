package com.mod.rbh.items.renderer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ExtendedRifleItemRenderer {
    private static HandStatus main = new HandStatus(0, null);
    private static HandStatus off = new HandStatus(0, null);

    public static void startUnequip(int timeToRender, ItemStack gun, InteractionHand hnd) {
        if (hnd.equals(InteractionHand.MAIN_HAND)) {
            main.rifle = gun;
            main.ticksToKeepRendering = timeToRender;
        } else {
            off.rifle = gun;
            off.ticksToKeepRendering = timeToRender;
        }
    }

    public static void tick() {
        main.tick();
        off.tick();
    }

    public static ItemStack getCurrentRifle(InteractionHand hand1) {
        if (hand1.equals(InteractionHand.MAIN_HAND)) {
            return main.rifle;
        } else {
            return off.rifle;
        }
    }

    public static boolean shouldRender(InteractionHand hand1) {
        if (hand1.equals(InteractionHand.MAIN_HAND)) {
            return main.ticksToKeepRendering > 0;
        } else {
            return off.ticksToKeepRendering > 0;
        }
    }

    private static class HandStatus {
        private int ticksToKeepRendering = 0;
        private ItemStack rifle = null;

        public HandStatus(int timeToRender, ItemStack gun) {
            ticksToKeepRendering = timeToRender;
            rifle = gun;
        }

        public ItemStack getCurrentRifle(InteractionHand hand1) {
            return rifle;
        }

        public void tick() {
            ticksToKeepRendering--;
        }
    }
}
