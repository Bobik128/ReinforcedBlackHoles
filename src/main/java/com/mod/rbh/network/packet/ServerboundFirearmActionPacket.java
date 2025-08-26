package com.mod.rbh.network.packet;

import com.mod.rbh.items.SingularityRifle;
import com.mod.rbh.network.RBHPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

public record ServerboundFirearmActionPacket(SingularityRifle.Action action) implements RBHPacket {

    public ServerboundFirearmActionPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(SingularityRifle.Action.class));
    }

    @Override
    public void rootEncode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
    }

    @Override
    public void handle(Executor exec, PacketListener listener, @Nullable ServerPlayer sender) {
        if (sender == null)
            return;
        ItemStack mainhandItem = sender.getMainHandItem();
        if (mainhandItem.getItem() instanceof SingularityRifle mainFirearm) {
            switch (this.action) {
                case RELOAD -> mainFirearm.onReload(mainhandItem, sender);
                case CHARGE_START -> mainFirearm.chargeStart(mainhandItem, sender);
                case CHARGE_END -> mainFirearm.chargeEnd(mainhandItem, sender);
            }
        }
    }

}