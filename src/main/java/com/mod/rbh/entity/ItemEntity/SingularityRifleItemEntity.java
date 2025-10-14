package com.mod.rbh.entity.ItemEntity;

import com.mod.rbh.entity.RBHEntityTypes;
import com.mod.rbh.sound.LoopingSound;
import com.mod.rbh.sound.RBHSounds;
import com.mod.rbh.utils.FirearmMode;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SingularityRifleItemEntity extends ItemEntity {
    private static final int LIFETIME = 6000;

    @OnlyIn(Dist.CLIENT) private LoopingSound soundInstance;

    public SingularityRifleItemEntity(EntityType<? extends ItemEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SingularityRifleItemEntity(Level pLevel, double pPosX, double pPosY, double pPosZ, ItemStack pItemStack, double pDeltaX, double pDeltaY, double pDeltaZ) {
        this(RBHEntityTypes.RIFLE_ITEM.get(), pLevel);
        this.setPos(pPosX, pPosY, pPosZ);
        this.setDeltaMovement(pDeltaX, pDeltaY, pDeltaZ);
        this.setItem(pItemStack);
        this.lifespan = (pItemStack.getItem() == null ? LIFETIME : pItemStack.getEntityLifespan(pLevel));
    }

    public SingularityRifleItemEntity(ItemEntity pOther) {
        super(RBHEntityTypes.RIFLE_ITEM.get(), pOther.level());
        this.setItem(pOther.getItem().copy());
        this.copyPosition(pOther);
        this.lifespan = pOther.lifespan;
        this.setDeltaMovement(pOther.getDeltaMovement());
        this.setDefaultPickUpDelay();
    }

    public SingularityRifleItemEntity(Level pLevel, double pPosX, double pPosY, double pPosZ, ItemStack pItemStack) {
        this(pLevel, pPosX, pPosY, pPosZ, pItemStack, pLevel.random.nextDouble() * 0.2D - 0.1D, 0.2D, pLevel.random.nextDouble() * 0.2D - 0.1D);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        if (soundInstance == null || soundInstance.isStopped()) {
            soundInstance = new LoopingSound(RBHSounds.ELECTRIC_BUZZ_MONO.get(), SoundSource.NEUTRAL, this, FirearmMode.getVolume(getItem()) + 0.01f);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
        }
        soundInstance.setVolume(FirearmMode.getVolume(getItem()));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            clientTick();
        }
    }
}
