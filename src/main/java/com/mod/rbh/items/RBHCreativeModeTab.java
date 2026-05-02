package com.mod.rbh.items;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RBHCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReinforcedBlackHoles.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_MODE_TAB =
            CREATIVE_MODE_TABS.register("rbh_tab", () ->
                    CreativeModeTab.builder()
                            .icon(() -> new ItemStack(RBHBlocks.HOLE_SHOWCASE.get().asItem()))
                            .title(Component.translatable("rbh.creativetab.tab"))
                            .displayItems((parameters, output) -> {
                                output.accept(RBHBlocks.HOLE_SHOWCASE.get().asItem());
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}