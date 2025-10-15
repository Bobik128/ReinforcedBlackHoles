package com.mod.rbh.sound;

import com.mod.rbh.items.RBHItems;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.utils.FirearmMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

public class ClientSoundHandler {
    private static ItemHoldLoopingSound currentLoopSound = null;

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.ClientTickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean holding = mc.player.getMainHandItem().getItem() instanceof SingularityRifle;
        ItemStack stack = mc.player.getMainHandItem();

        if (holding) {
            if (currentLoopSound == null || currentLoopSound.isStopped()) {
                currentLoopSound = new ItemHoldLoopingSound(
                        RBHSounds.ELECTRIC_BUZZ_STEREO.get(), mc.player, RBHItems.SINGULARITY_RIFLE.get(), FirearmMode.getVolume(stack) + 0.01f);
                mc.getSoundManager().play(currentLoopSound);
            }
            currentLoopSound.setVolume(FirearmMode.getVolume(stack));
        } else {
            if (currentLoopSound != null && !currentLoopSound.isStopped()) {
                currentLoopSound.remove();
                currentLoopSound = null;
            }
        }
    }
}
