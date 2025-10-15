package com.mod.rbh.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityBoundSound extends AbstractTickableSoundInstance {
    Entity entity;
    public boolean enabled = true;

    public EntityBoundSound(SoundEvent sound, SoundSource category, Entity entity, float volume) {
        super(sound, category, SoundInstance.createUnseededRandom());
        this.entity = entity;

        this.x = this.entity.getEyePosition().x;
        this.y = this.entity.getEyePosition().y;
        this.z = this.entity.getEyePosition().z;

        this.looping = false;
        this.delay = 0;
        this.volume = volume;
    }

    public void remove() {
        this.stop();
    }

    @Override
    public boolean canPlaySound() {
        return !this.isStopped();
    }

    @Override
    public void tick() {
        if (!enabled) {
            stop();
            return;
        }
        if (entity != null) {
            this.x = entity.getEyePosition().x;
            this.y = entity.getEyePosition().y;
            this.z = entity.getEyePosition().z;
        } else {
            stop();
        }
        enabled = false;
    }

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
