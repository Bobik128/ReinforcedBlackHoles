package com.mod.rbh.remix;

import com.mod.rbh.api.HoldAttackKeyInteraction;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.network.packet.ServerboundSetAttackKeyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class RBHClientRemix {
    public static void handleAttackKeybinds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainhandItem = mc.player.getMainHandItem();
        if (mainhandItem.getItem() instanceof HoldAttackKeyInteraction hold && !hold.isHoldingAttackKey(mainhandItem, mc.player)) {
            RBHNetwork.sendToServer(new ServerboundSetAttackKeyPacket(true));
            hold.onPressAttackKey(mainhandItem, mc.player);
        }
    }

    public static void interruptAttack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainhandItem = mc.player.getMainHandItem();
        if (mainhandItem.getItem() instanceof HoldAttackKeyInteraction hold && hold.isHoldingAttackKey(mainhandItem, mc.player)) {
            RBHNetwork.sendToServer(new ServerboundSetAttackKeyPacket(false));
            hold.onReleaseAttackKey(mainhandItem, mc.player);
        }
    }
}
