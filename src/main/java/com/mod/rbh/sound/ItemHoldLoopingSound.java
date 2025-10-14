package com.mod.rbh.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ItemHoldLoopingSound extends AbstractTickableSoundInstance {
    private final Player player;
    private final Item targetItem;

    public ItemHoldLoopingSound(SoundEvent soundEvent, Player player, Item targetItem, float volume) {
        super(soundEvent, SoundSource.NEUTRAL, RandomSource.create());
        this.player = player;
        this.targetItem = targetItem;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = 1.0f;
    }

    public void remove() {
        this.stop();
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void tick() {
        // stop if player no longer holds the item
        if (player.isRemoved() ||
                !(player.getMainHandItem().getItem() == targetItem)) {
            this.stop();
        } else {
            // update position so it follows player
            this.x = (float) player.getX();
            this.y = (float) player.getY();
            this.z = (float) player.getZ();
        }
    }
}

