package com.mod.rbh.network.packet;

import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import com.mod.rbh.network.RBHPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

public class ServerBoundUpdateHoleShowcasePacket implements RBHPacket {
    private final ResourceKey<Level> levelKey;
    private BlockPos pos;
    private HoleShowcaseBlockEntity.HoleShowcaseConfig config;

    public ServerBoundUpdateHoleShowcasePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), new HoleShowcaseBlockEntity.HoleShowcaseConfig(buf), ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()));
    }

    public ServerBoundUpdateHoleShowcasePacket(BlockPos pos, HoleShowcaseBlockEntity.HoleShowcaseConfig config, ResourceKey<Level> levelKey) {
        this.pos = pos;
        this.config = config;
        this.levelKey = levelKey;
    }

    @Override
    public void rootEncode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        config.toBytes(buf);
        buf.writeResourceLocation(levelKey.location());
    }

    @Override
    public void handle(Executor exec, PacketListener listener, @Nullable ServerPlayer sender) {
        exec.execute(() -> {
            ServerLevel level = sender.server.getLevel(levelKey);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HoleShowcaseBlockEntity hsbe) {
                hsbe.config = config;
                hsbe.setChanged();
            }
        });
    }
}
