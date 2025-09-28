package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<Item> SINGULARITY_RIFLE = ITEMS.register("singularity_rifle",
            () -> new SingularityRifle(new Item.Properties()));

    public static final RegistryObject<Item> SINGULARITY_BATTERY = ITEMS.register("singularity_battery",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SINGULARITY_BATTERY_EMPTY = ITEMS.register("singularity_battery_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SINGULARITY_BATTERY_INCOMPLETE = ITEMS.register("singularity_battery_incomplete",
                    () -> new SequencedAssemblyItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
