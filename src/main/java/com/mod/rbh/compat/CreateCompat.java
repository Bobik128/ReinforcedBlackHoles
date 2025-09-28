package com.mod.rbh.compat;

import net.minecraftforge.fml.ModList;

public class CreateCompat {
    public static final String CREATE_MODID = "create";
    private static boolean isCreateLoaded = false;

    public static boolean isCreateLoaded() {
        return isCreateLoaded;
    }

    public static void init() {
        isCreateLoaded = ModList.get().isLoaded(CREATE_MODID);
    }
}
