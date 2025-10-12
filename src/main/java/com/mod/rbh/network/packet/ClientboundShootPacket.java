package com.mod.rbh.network.packet;

import com.mod.rbh.client.RifleShootAnimHelper;
import com.mod.rbh.network.RBHPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

public record ClientboundShootPacket(long shoterId, int chargeLevel) implements RBHPacket {

    public ClientboundShootPacket(FriendlyByteBuf buf) {
        this(buf.readVarLong(), buf.readVarInt());
    }

    @Override
    public void rootEncode(FriendlyByteBuf buf) {
        buf.writeVarLong(shoterId);
        buf.writeVarInt(chargeLevel);
    }

    @Override
    public void handle(Executor exec, PacketListener listener, @Nullable ServerPlayer sender) {
        exec.execute(() -> RifleShootAnimHelper.addShootingRifle(shoterId, chargeLevel));
    }
}