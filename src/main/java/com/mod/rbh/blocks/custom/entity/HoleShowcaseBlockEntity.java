package com.mod.rbh.blocks.custom.entity;

import com.mod.rbh.entity.TestBlackHole;
import com.mod.rbh.network.RBHNetwork;
import com.mod.rbh.network.packet.ClientBoundOpenGuiPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

    public HoleShowcaseConfig config = new HoleShowcaseConfig(
            1.0f,        // effectRadius
            0.5f,        // holeRadius
            false,       // rainbow
            0xFFFF00,    // color
            4.0f,        // effectExponent
            new Vec3(1, 0, 0), // stretchDir
            0.0f,        // stretchStrength
            0.0f         // height
    );

    public HoleShowcaseBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RBHBlockEntities.HOLE_SHOWCASE_BE.get(), pPos, pBlockState);
    }

    public void openGUI(Level level, BlockPos pos, Player player) {
        RBHNetwork.sendToPlayer(new ClientBoundOpenGuiPacket(pos, config), (ServerPlayer) player);
    }

    public void remove() {
        if (this.level instanceof ServerLevel serverLevel && this.holeUUID != null) {
            serverLevel.getEntity(holeUUID).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        // UUID
        if (holeUUID != null) {
            tag.putUUID("HoleUUID", holeUUID);
        }

        // Floats
        tag.putFloat("EffectRadius", config.effectRadius);
        tag.putFloat("HoleRadius", config.holeRadius);
        tag.putFloat("EffectExponent", config.effectExponent);
        tag.putFloat("StretchStrength", config.stretchStrength);

        // Boolean
        tag.putBoolean("Rainbow", config.rainbow);

        // Int
        tag.putInt("Color", config.color);

        // Vec3
        tag.putDouble("StretchDirX", config.stretchDir.x);
        tag.putDouble("StretchDirY", config.stretchDir.y);
        tag.putDouble("StretchDirZ", config.stretchDir.z);

        tag.putFloat("Height", config.height);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID("HoleUUID")) {
            holeUUID = tag.getUUID("HoleUUID");
        } else {
            holeUUID = null;
        }

        float effectRadius = tag.getFloat("EffectRadius");
        float holeRadius = tag.getFloat("HoleRadius");
        float effectExponent = tag.getFloat("EffectExponent");
        float stretchStrength = tag.getFloat("StretchStrength");
        boolean rainbow = tag.getBoolean("Rainbow");
        int color = tag.getInt("Color");
        float height = tag.getFloat("Height");

        double x = tag.getDouble("StretchDirX");
        double y = tag.getDouble("StretchDirY");
        double z = tag.getDouble("StretchDirZ");
        Vec3 stretchDir = new Vec3(x, y, z);

        this.config = new HoleShowcaseConfig(
                effectRadius,
                holeRadius,
                rainbow,
                color,
                effectExponent,
                stretchDir,
                stretchStrength,
                height
        );
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

            if (blackHole != null)
                config.applyValues(blackHole, pPos);

            if (blackHole == null) {
                blackHole = new TestBlackHole(pPos.getCenter().add(0, config.height + 2, 0), this.level, config.holeRadius, config.effectRadius);
                config.applyValues(blackHole, pPos);
                level.addFreshEntity(blackHole);
                this.holeUUID = blackHole.getUUID();
                this.setChanged();
            }
        }
    }

    public static class HoleShowcaseConfig {
        public float effectRadius;
        public float holeRadius;
        public boolean rainbow;
        public int color;
        public float effectExponent;
        public Vec3 stretchDir;
        public float stretchStrength;
        public float height;

        // Main constructor
        public HoleShowcaseConfig(
                float effectRadius,
                float holeRadius,
                boolean rainbow,
                int color,
                float effectExponent,
                Vec3 stretchDir,
                float stretchStrength,
                float height
        ) {
            this.effectRadius = effectRadius;
            this.holeRadius = holeRadius;
            this.rainbow = rainbow;
            this.color = color;
            this.effectExponent = effectExponent;
            this.stretchDir = stretchDir;
            this.stretchStrength = stretchStrength;
            this.height = height;
        }

        public void applyValues(TestBlackHole hole, BlockPos pos) {
            hole.setEffectSize(effectRadius);
            hole.setSize(holeRadius);
            hole.setRainbow(rainbow);
            hole.setColor(color);
            hole.setEffectExponent(effectExponent);
            hole.setStretchDir(stretchDir.toVector3f());
            hole.setStretchStrength(stretchStrength);
            hole.setPos(pos.getCenter().add(0, height + 2, 0));
        }

        // Network helpers
        public HoleShowcaseConfig(FriendlyByteBuf buf) {
            this.setFromBuf(buf);
        }

        public void setFromBuf(FriendlyByteBuf buf) {
            this.effectRadius     = buf.readFloat();
            this.holeRadius       = buf.readFloat();
            this.rainbow          = buf.readBoolean();
            this.color            = buf.readInt();
            this.effectExponent   = buf.readFloat();

            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            this.stretchDir       = new Vec3(x, y, z);

            this.stretchStrength  = buf.readFloat();
            this.height           = buf.readFloat();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeFloat(effectRadius);
            buf.writeFloat(holeRadius);
            buf.writeBoolean(rainbow);
            buf.writeInt(color);
            buf.writeFloat(effectExponent);

            buf.writeDouble(stretchDir.x);
            buf.writeDouble(stretchDir.y);
            buf.writeDouble(stretchDir.z);

            buf.writeFloat(stretchStrength);
            buf.writeFloat(height);
        }
    }
}
