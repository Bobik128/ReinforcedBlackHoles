package com.mod.rbh;

import com.mod.rbh.blocks.RBHBlocks;
import com.mod.rbh.blocks.custom.entity.RBHBlockEntities;
import com.mod.rbh.datagen.DataGenerators;
import com.mod.rbh.entity.RBHEntityTypes;
import com.mod.rbh.items.RBHCreativeModeTab;
import com.mod.rbh.items.RBHItems;
import com.mod.rbh.network.RBHNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ReinforcedBlackHoles.MODID)
public class ReinforcedBlackHoles {
    public static final String MODID = "rbh";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReinforcedBlackHoles(IEventBus modEventBus, ModContainer modContainer) {
        IEventBus neoForgeBus = NeoForge.EVENT_BUS;

        modEventBus.addListener(this::commonSetup);
        RBHNetwork.register(modEventBus);
        ClientConfig.register(modEventBus);

        DataGenerators.register(modEventBus);

        RBHItems.register(modEventBus);
        RBHCreativeModeTab.register(modEventBus);
        RBHBlocks.register(modEventBus);
        RBHEntityTypes.register(modEventBus);
        RBHBlockEntities.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            RBHClientNeoForge.init(modEventBus, neoForgeBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}