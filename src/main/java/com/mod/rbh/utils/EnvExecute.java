package com.mod.rbh.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public class EnvExecute {

    public static void runOnClient(Runnable toRun) {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            toRun.run();
        }
    }
}