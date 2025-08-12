package com.mod.rbh.api;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface IShaderInstance {
    static IShaderInstance fromShaderInstance(@NotNull ShaderInstance shader) {
        return (IShaderInstance) shader;
    }

    void setBlackHoleCenter(Vec3 center);
}
