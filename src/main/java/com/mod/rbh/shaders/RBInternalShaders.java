package com.mod.rbh.shaders;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReinforcedBlackHoles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class RBInternalShaders {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ShaderInstance renderTypeBlackHole;

    public static ShaderInstance getRenderTypeBlackHole() {
        return renderTypeBlackHole;
    }

    public static void setRenderTypeBlackHole(ShaderInstance blackHoleShader) {
        RBInternalShaders.renderTypeBlackHole = blackHoleShader;
    }

    @SubscribeEvent
    public static void FMLClientSetup(FMLClientSetupEvent event) {
        PostEffectRegistry.registerMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent e) {
        try {
            e.registerShader(new ShaderInstance(e.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "rendertype_black_hole"), DefaultVertexFormat.POSITION_COLOR_TEX), RBInternalShaders::setRenderTypeBlackHole);
            LOGGER.info("registered internal shaders");
        } catch (IOException exception) {
            LOGGER.error("could not register internal shaders");
            exception.printStackTrace();
        }
    }
}
