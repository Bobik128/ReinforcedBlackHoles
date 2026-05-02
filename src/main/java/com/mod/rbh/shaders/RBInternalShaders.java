package com.mod.rbh.shaders;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

import java.io.IOException;

public class RBInternalShaders {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ShaderInstance renderTypeBlackHole;

    public static ShaderInstance getRenderTypeBlackHole() {
        return renderTypeBlackHole;
    }

    public static void setRenderTypeBlackHole(ShaderInstance blackHoleShader) {
        RBInternalShaders.renderTypeBlackHole = blackHoleShader;
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RBInternalShaders::clientSetup);
        modBus.addListener(RBInternalShaders::registerShaders);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        PostEffectRegistry.registerMutableEffect(RBHRenderTypes.BLACK_HOLE_POST_SHADER);
    }

    private static void registerShaders(final RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(
                                    ReinforcedBlackHoles.MODID,
                                    "rendertype_black_hole"
                            ),
                            DefaultVertexFormat.NEW_ENTITY
                    ),
                    RBInternalShaders::setRenderTypeBlackHole
            );

            LOGGER.info("Registered internal shaders");
        } catch (IOException exception) {
            LOGGER.error("Could not register internal shaders", exception);
        }
    }
}