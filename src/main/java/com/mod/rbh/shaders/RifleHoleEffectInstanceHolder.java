package com.mod.rbh.shaders;

import com.mod.rbh.ReinforcedBlackHoles;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RifleHoleEffectInstanceHolder {
    private static final Map<Integer, PostEffectRegistry.HoleEffectInstance> effects = new HashMap<>();
    private static final Map<Integer, Integer> timers = new HashMap<>();

    // Cached list to avoid allocating every tick.
    private static final List<Integer> toRemove = new ArrayList<>();

    private static int effectCounter = 0;

    public static void clientTick() {
        for (Map.Entry<Integer, PostEffectRegistry.HoleEffectInstance> entry : effects.entrySet()) {
            Integer timer = timers.get(entry.getKey());

            if (timer == null || timer <= 0) {
                toRemove.add(entry.getKey());
                continue;
            }

            timers.put(entry.getKey(), timer - 1);
        }

        for (Integer id : toRemove) {
            effects.remove(id);
            timers.remove(id);
        }

        toRemove.clear();
    }

    public static void resetEffectCounter(RenderFrameEvent.Post event) {
        effectCounter = 0;
    }
}