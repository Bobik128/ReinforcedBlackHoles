package com.mod.rbh.blocks.custom.entity;

import com.mod.rbh.entity.TestBlackHole;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.network.packet.ClientBoundOpenGuiPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.UUID;

public class HoleShowcaseBlockEntity extends BlockEntity {
    protected static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    public UUID holeUUID = null;
    public float effectRadius = 1.0f;
    public float holeRadius = 0.5f;
    public boolean rainbow = false;
    public int color = 0xFFFF00;
    public float effectExponent = 4.0f;
    public Vec3 stretchDir = new Vec3(1.0, 0.0,0.0);
    public float stretchStrength = 0.0f;
    public float height = 2.0f;

    public HoleShowcaseBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RBHBlockEntities.HOLE_SHOWCASE_BE.get(), pPos, pBlockState);
    }

    public void openGUI(Level level, BlockPos pos, Player player) {
        RBHNetwork.sendToPlayer(new ClientBoundOpenGuiPacket(pos), (ServerPlayer) player);
    }

    public void remove() {
        if (this.level instanceof ServerLevel serverLevel && this.holeUUID != null) {
            serverLevel.getEntity(holeUUID).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // UUID
        if (holeUUID != null) {
            tag.putUUID("HoleUUID", holeUUID);
        }

        // Floats
        tag.putFloat("EffectRadius", effectRadius);
        tag.putFloat("HoleRadius", holeRadius);
        tag.putFloat("EffectExponent", effectExponent);
        tag.putFloat("StretchStrength", stretchStrength);

        // Boolean
        tag.putBoolean("Rainbow", rainbow);

        // Int
        tag.putInt("Color", color);

        // Vec3
        tag.putDouble("StretchDirX", stretchDir.x);
        tag.putDouble("StretchDirY", stretchDir.y);
        tag.putDouble("StretchDirZ", stretchDir.z);
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // UUID
        if (tag.hasUUID("HoleUUID")) {
            holeUUID = tag.getUUID("HoleUUID");
        } else {
            holeUUID = null;
        }

        // Floats
        effectRadius = tag.getFloat("EffectRadius");
        holeRadius = tag.getFloat("HoleRadius");
        effectExponent = tag.getFloat("EffectExponent");
        stretchStrength = tag.getFloat("StretchStrength");

        // Boolean
        rainbow = tag.getBoolean("Rainbow");

        // Int
        color = tag.getInt("Color");

        // Vec3
        double x = tag.getDouble("StretchDirX");
        double y = tag.getDouble("StretchDirY");
        double z = tag.getDouble("StretchDirZ");
        stretchDir = new Vec3(x, y, z);
    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!this.level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            TestBlackHole blackHole = null;

            if (this.holeUUID != null) {
                Entity e = serverLevel.getEntity(this.holeUUID);
                if (e instanceof TestBlackHole bh) {
                    blackHole = bh;
                }
            }

            if (blackHole == null) {
                blackHole = new TestBlackHole(pPos.getCenter().add(0, height, 0), this.level, holeRadius, effectRadius);
                blackHole.setColor(color);
                blackHole.setRainbow(rainbow);
                blackHole.setStretchDir(stretchDir.toVector3f());
                blackHole.setEffectExponent(effectExponent);
                blackHole.setStretchStrength(stretchStrength);
                level.addFreshEntity(blackHole);
                this.holeUUID = blackHole.getUUID();
            }
        }
    }
}
