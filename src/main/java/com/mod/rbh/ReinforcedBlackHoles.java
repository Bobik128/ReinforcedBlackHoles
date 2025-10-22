package com.mod.rbh;

import com.mod.rbh.compat.CreateCompat;
import com.mod.rbh.entity.RBHEntityTypes;
import com.mod.rbh.items.RBHCreativeModeTab;
import com.mod.rbh.items.RBHItems;
import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.recipe.RBHRecipes;
import com.mod.rbh.sound.RBHSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(ReinforcedBlackHoles.MODID)
public class ReinforcedBlackHoles
{
    public static final String MODID = "rbh";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReinforcedBlackHoles(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modEventBus.addListener(this::commonSetup);

        GeckoLib.initialize();

        RBHItems.register(modEventBus);
        RBHCreativeModeTab.register(modEventBus);
        RBHEntityTypes.register(modEventBus);
        RBHRecipes.register(modEventBus);
        RBHSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RBHClientForge.init(modEventBus, forgeBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RBHNetwork.init();
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            RBHClient.onClientSetup();
        }

        @SubscribeEvent
        public void onRenderPlayer(RenderLivingEvent.Pre<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> event) {
            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
            ItemStack main = player.getMainHandItem();

            if (main.getItem() instanceof SingularityRifle) {
                PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

                model.rightArm.xRot = -90.0F;
                model.leftArm.xRot = -90.0F;
                model.rightArm.yRot = -20.0F;
                model.leftArm.yRot = 20.0F;
            }
        }

    }
}
