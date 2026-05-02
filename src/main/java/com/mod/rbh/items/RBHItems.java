package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RBHItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ReinforcedBlackHoles.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}