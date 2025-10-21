package com.mod.rbh.utils;

import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.sound.EntityBoundSound;
import com.mod.rbh.sound.RBHSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class FirearmModeClient {
    private static final HashMap<Long, EntityBoundSound> reloadSounds = new HashMap<>();

    public static void clientTick(FirearmMode mode, ItemStack itemStack, LivingEntity entity, boolean isSelected, long id, int actionTime) {
        if (!isSelected && FirearmDataUtils.getAction(itemStack) == SingularityRifle.Action.RELOAD) {
            if (reloadSounds.get(id) != null) {
                reloadSounds.get(id).remove();
                reloadSounds.remove(id);
            }
        }
        if (reloadSounds.get(id) != null && (reloadSounds.get(id).isStopped() || FirearmDataUtils.getAction(itemStack) != SingularityRifle.Action.RELOAD)) {
            reloadSounds.get(id).remove();
            reloadSounds.remove(id);
        }

        if (actionTime > 0) {
            if (FirearmDataUtils.getAction(itemStack) == SingularityRifle.Action.RELOAD) {
                if (reloadSounds.containsKey(id))
                    reloadSounds.get(id).enabled = true;
            }
        }

        if (FirearmDataUtils.getAction(itemStack) == SingularityRifle.Action.RELOAD && !reloadSounds.containsKey(id)) {
            EntityBoundSound si = new EntityBoundSound(RBHSounds.RIFLE_RELOAD.get(), SoundSource.NEUTRAL, entity, 1.0f);
            reloadSounds.put(GeoItem.getId(itemStack), si);
            Minecraft.getInstance().getSoundManager().play(si);
        }

        int runningTime = mode.getRunTime(itemStack, entity);
        if (runningTime > 0) {
            --runningTime;
            mode.setRTime(itemStack, entity, runningTime);
        }

        boolean wasRunning = FirearmDataUtils.isRunning(itemStack);
        boolean isNowRunning = mode.isRunning(itemStack, entity);

        if (isNowRunning && !wasRunning)
            mode.startRunning(itemStack, entity);
        if (!isNowRunning && wasRunning)
            mode.stopRunning(itemStack, entity);
    }
}
