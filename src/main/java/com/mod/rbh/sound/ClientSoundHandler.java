package com.mod.rbh.sound;

import com.mod.rbh.items.RBHItems;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.items.renderer.SingularityRifleRenderer;
import com.mod.rbh.utils.FirearmMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientSoundHandler {
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        List<Long> toRemove = new ArrayList<>();
        for (Map.Entry<Long, ItemLoopingSound> entry : SingularityRifleRenderer.sounds.entrySet()) {
            if (entry.getValue().isStopped()) toRemove.add(entry.getKey());
        }

        for (Long l : toRemove) {
            SingularityRifleRenderer.sounds.remove(l);
        }
    }
}
