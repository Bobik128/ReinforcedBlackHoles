package com.mod.rbh.mixin.client;

import com.mod.rbh.api.IPostPass;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PostPass.class)
public class PostPassMixin implements IPostPass {

    @Unique
    private Consumer<PostPass> reinforcedBreakable$toRun = (i) -> {};

    @Inject(method = "process", at = @At("HEAD"))
    private void onProcess(float pPartialTicks, CallbackInfo ci) {
        reinforcedBreakable$toRun.accept((PostPass) (Object) this);
    }

    @Override
    public void toRunOnProcess(Consumer<PostPass> toRun) {
        this.reinforcedBreakable$toRun = toRun;
    }
}
