package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.compat.CreateCompat;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<Item> SINGULARITY_BATTERY = ITEMS.register("singularity_battery",
            () -> new SingularityBattery(new Item.Properties()));

    public static final RegistryObject<Item> SINGULARITY_BATTERY_EMPTY = ITEMS.register("singularity_battery_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static RegistryObject<Item> SINGULARITY_BATTERY_INCOMPLETE;

    public static final RegistryObject<Item> SINGULARITY_RIFLE = ITEMS.register("singularity_rifle",
            () -> new SingularityRifle(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

        // Optional Create mod integration
        if (CreateCompat.isCreateLoaded()) {
            registerCreateItems();
        }
    }

    private static void registerCreateItems() {
        try {
            // Dynamically load class so JVM won’t fail if Create is missing
            Class<?> seqItemClass = Class.forName("com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem");
            Item item = (Item) seqItemClass
                    .getConstructor(Item.Properties.class)
                    .newInstance(new Item.Properties().stacksTo(1));
            SINGULARITY_BATTERY_INCOMPLETE = ITEMS.register("singularity_battery_incomplete", () -> item);
        } catch (Exception e) {
            ReinforcedBlackHoles.LOGGER.warn("Create detected, but failed to register SequencedAssemblyItem:", e);
        }
    }
}