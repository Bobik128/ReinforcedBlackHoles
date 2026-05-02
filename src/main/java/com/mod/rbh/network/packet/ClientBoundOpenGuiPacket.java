package com.mod.rbh.network.packet;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import com.mod.rbh.client.screen.ClientScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundOpenGuiPacket(
        BlockPos pos,
        HoleShowcaseBlockEntity.HoleShowcaseConfig config
) implements CustomPacketPayload {

    public static final Type<ClientBoundOpenGuiPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ReinforcedBlackHoles.MODID,
                    "open_hole_showcase_gui"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundOpenGuiPacket> STREAM_CODEC =
            StreamCodec.of(
                    ClientBoundOpenGuiPacket::encode,
                    ClientBoundOpenGuiPacket::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, ClientBoundOpenGuiPacket packet) {
        buf.writeBlockPos(packet.pos);
        packet.config.toBytes(buf);
    }

    private static ClientBoundOpenGuiPacket decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        HoleShowcaseBlockEntity.HoleShowcaseConfig config =
                new HoleShowcaseBlockEntity.HoleShowcaseConfig(buf);

        return new ClientBoundOpenGuiPacket(pos, config);
    }

    public static void handle(ClientBoundOpenGuiPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                ClientScreenHandler.openHoleShowcaseGui(packet.pos, packet.config)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}