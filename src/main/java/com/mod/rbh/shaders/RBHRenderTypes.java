package com.mod.rbh.shaders;

import com.mod.rbh.compat.ShaderCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Function;

public class RBHRenderTypes extends RenderType {
    public static final ResourceLocation BLACK_HOLE_POST_SHADER = ResourceLocation.parse("rbh:shaders/post/black_hole.json");

    protected static final ShaderStateShard RENDERTYPE_BLACK_HOLE_SHADER =
            new ShaderStateShard(RBInternalShaders::getRenderTypeBlackHole);


    public RBHRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
    public static RenderType getBlackHole(ResourceLocation tex, @Nullable RenderTarget rt) {
        final FboGuard guard = new FboGuard(); // your class

        return (RenderType) create("black_hole",
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, true,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_BLACK_HOLE_SHADER)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setTextureState(new TextureStateShard(tex, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setOutputState(new OutputStateShard("black_hole_target",
                                () -> {
                                    if (rt == null) return;
                                    guard.save();
                                    // *** Critical: skip depth copy in hand phase ***
                                    if (PostEffectRegistry.PhaseScope.current() != PostEffectRegistry.RenderPhase.AFTER_ARM || !ShaderCompat.shadersEnabled()) {
                                        // safe only in world phase
                                        rt.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
                                    }
                                    rt.bindWrite(false);
                                },
                                () -> {
                                    if (rt == null) return;
                                    // restore previous FBO + viewport/scissor, NOT main
                                    guard.restore();
                                }))
                        .createCompositeState(false));
    }


//    public static RenderType getBlackHole(ResourceLocation locationIn, @Nullable RenderTarget renderTarget) {
//        return (RenderType)create("black_hole", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
//                .setShaderState(RENDERTYPE_BLACK_HOLE_SHADER)
//                .setCullState(RenderStateShard.CULL)
//                .setTextureState((EmptyTextureStateShard)new TextureStateShard(locationIn, false, false))
//                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
//                .setOutputState(new OutputStateShard("black_hole_target",
//                        () -> {
//                            if (renderTarget != null) {
////                                if (Minecraft.getInstance().levelRenderer.getItemEntityTarget() != null) {
////                                    renderTarget.copyDepthFrom(Minecraft.getInstance().levelRenderer.getItemEntityTarget());
////                                } else {
//                                    renderTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
////                                }
//                                renderTarget.bindWrite(false);
//                            }
//                        },
//                        () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false))
//                )
//                .createCompositeState(false));
//    }

    public static RenderType getEmissiveRenderType(ResourceLocation texture) {
        TextureStateShard textureState = new TextureStateShard(texture, false, false);

        CompositeState compositeState = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(textureState)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setOverlayState(OVERLAY)
                .setLightmapState(NO_LIGHTMAP) // disable lightmap, full emissive
                .createCompositeState(false);

        return RenderType.create("reinforced_emissive",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true, // needs sorting
                true, // affects outline rendering
                compositeState);
    }
}
