package com.mod.rbh.shaders;

import com.ibm.icu.impl.Pair;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RifleHoleEffectInstanceHolder {
    private static Map<ItemStack, PostEffectRegistry.HoleEffectInstance> effects = new HashMap<>();
    private static Map<ItemStack, Integer> timers = new HashMap<>();

    private static final List<ItemStack> toRemove = new ArrayList<>();// caching it for effectivity
    public static void clientTick() {
        for (Map.Entry<ItemStack, PostEffectRegistry.HoleEffectInstance> entry : effects.entrySet()) {
            if (timers.get(entry.getKey()) <= 0) {
                toRemove.add(entry.getKey());
                continue;
            }
            timers.put(entry.getKey(), timers.get(entry.getKey()) - 1);
        }

        for (ItemStack stack : toRemove) {
            effects.remove(stack);
            timers.remove(stack);
        }

        toRemove.clear();
    }

    public static PostEffectRegistry.HoleEffectInstance getEffect(ItemStack item) {
        timers.put(item, 20);
        return effects.computeIfAbsent(item, (itemStack) -> PostEffectRegistry.HoleEffectInstance.createEffectInstance());
    }
}
