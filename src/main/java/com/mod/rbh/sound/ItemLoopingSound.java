package com.mod.rbh.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ItemLoopingSound extends AbstractTickableSoundInstance {
    public boolean enabled = true;

    public ItemLoopingSound(SoundEvent sound, SoundSource category, float volume) {
        super(sound, category, SoundInstance.createUnseededRandom());

        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void remove() {
        this.stop();
    }

    public void setPos(Vec3 pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    @Override
    public void tick() {
        if (!enabled) {
            stop();
        }
        enabled = false;
    }

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
