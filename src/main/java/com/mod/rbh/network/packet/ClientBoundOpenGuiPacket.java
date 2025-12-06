package com.mod.rbh.network.packet;

import com.mod.rbh.client.RifleShootAnimHelper;
import com.mod.rbh.client.screen.ClientScreenHandler;
import com.mod.rbh.network.RBHPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

public class ClientBoundOpenGuiPacket implements RBHPacket {
    private BlockPos pos;

    public ClientBoundOpenGuiPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public ClientBoundOpenGuiPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void rootEncode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(Executor exec, PacketListener listener, @Nullable ServerPlayer sender) {
        exec.execute(() -> ClientScreenHandler.openHoleShowcaseGui(pos));
    }
}
