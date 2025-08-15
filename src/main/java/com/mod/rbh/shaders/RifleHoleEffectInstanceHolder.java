package com.mod.rbh.shaders;

import com.ibm.icu.impl.Pair;
import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class RifleHoleEffectInstanceHolder {
    private static Map<ItemStack, PostEffectRegistry.HoleEffectInstance> effects = new WeakHashMap<>();
    private static Map<ItemStack, Integer> timers = new WeakHashMap<>();

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

    public static @Nullable PostEffectRegistry.HoleEffectInstance getEffect(ItemStack item) {
        if (effects.size() < 80) {
            timers.put(item, 30);
            return effects.computeIfAbsent(item, (itemStack) -> PostEffectRegistry.HoleEffectInstance.createEffectInstance());
        }
        ReinforcedBlackHoles.LOGGER.warn("Too many rifle effects registered, skipping!");
        return null;
    }
}
