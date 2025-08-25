package com.mod.rbh.shaders;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.utils.FirearmDataUtils;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

public class RifleHoleEffectInstanceHolder {
    private static Map<Integer, PostEffectRegistry.HoleEffectInstance> effects = new HashMap<>();
    private static Map<Integer, Integer> timers = new HashMap<>();

    private static final List<Integer> toRemove = new ArrayList<>();// caching it for effectivity
    public static void clientTick() {
        for (Map.Entry<Integer, PostEffectRegistry.HoleEffectInstance> entry : effects.entrySet()) {
            if (timers.get(entry.getKey()) <= 0) {
                toRemove.add(entry.getKey());
                continue;
            }
            timers.put(entry.getKey(), timers.get(entry.getKey()) - 1);
        }

        for (Integer stack : toRemove) {
            effects.remove(stack);
            timers.remove(stack);
        }

        toRemove.clear();
    }

    private static int effectCounter = 0;

    public static void resetEffectCounter(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) effectCounter = 0;
    }

    public static @Nullable PostEffectRegistry.HoleEffectInstance getUniqueEffect() {
        if (effects.size() < 40) {
            effectCounter++;
            timers.put(effectCounter, 30);
            return effects.computeIfAbsent(effectCounter, (itemId) -> PostEffectRegistry.HoleEffectInstance.createEffectInstance());
        }
        ReinforcedBlackHoles.LOGGER.warn("Too many rifle effects registered, skipping!");
        return null;
    }
}
