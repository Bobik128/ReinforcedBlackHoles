package com.mod.rbh.compat;

import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

public class CreateCompat implements IConditionBuilder {
    public static final String CREATE_MODID = "create";

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MODID);
    }
}
