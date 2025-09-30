package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RBHCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_MODE_TABS.register("rbh.tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(RBHItems.SINGULARITY_BATTERY.get()))
                    .title(Component.translatable("rbh.creativetab.tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(RBHItems.SINGULARITY_RIFLE.get());
                        pOutput.accept(RBHItems.SINGULARITY_BATTERY.get());
                        pOutput.accept(RBHItems.SINGULARITY_BATTERY_EMPTY.get());
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
