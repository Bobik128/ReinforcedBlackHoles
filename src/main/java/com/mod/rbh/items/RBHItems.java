package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<Item> SINGULARITY_RIFLE = ITEMS.register("singularity_rifle",
            () -> new SingularityRifle(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
