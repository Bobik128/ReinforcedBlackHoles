package com.mod.rbh.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class LoopingSound extends AbstractTickableSoundInstance {
    private final Entity soundSource;

    public LoopingSound(SoundEvent sound, SoundSource category, Entity soundSource, float volume) {
        super(sound, category, SoundInstance.createUnseededRandom());
        this.soundSource = soundSource;

        this.x = (float) soundSource.getX();
        this.y = (float) soundSource.getY();
        this.z = (float) soundSource.getZ();

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

    @Override
    public void tick() {
        if (soundSource != null) {

            if (soundSource.isRemoved()) remove();

            this.x = soundSource.position().x;
            this.y = soundSource.position().y;
            this.z = soundSource.position().z;
        } else {
            stop();
        }
    }

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
