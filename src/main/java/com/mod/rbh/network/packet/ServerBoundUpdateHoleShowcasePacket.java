package com.mod.rbh.network.packet;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.custom.entity.HoleShowcaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundUpdateHoleShowcasePacket(
        BlockPos pos,
        HoleShowcaseBlockEntity.HoleShowcaseConfig config,
        ResourceKey<Level> levelKey
) implements CustomPacketPayload {

    public static final Type<ServerBoundUpdateHoleShowcasePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ReinforcedBlackHoles.MODID,
                    "update_hole_showcase"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUpdateHoleShowcasePacket> STREAM_CODEC =
            StreamCodec.of(
                    ServerBoundUpdateHoleShowcasePacket::encode,
                    ServerBoundUpdateHoleShowcasePacket::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, ServerBoundUpdateHoleShowcasePacket packet) {
        buf.writeBlockPos(packet.pos);
        packet.config.toBytes(buf);
        buf.writeResourceLocation(packet.levelKey.location());
    }

    private static ServerBoundUpdateHoleShowcasePacket decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();

        HoleShowcaseBlockEntity.HoleShowcaseConfig config =
                new HoleShowcaseBlockEntity.HoleShowcaseConfig(buf);

        ResourceKey<Level> levelKey = ResourceKey.create(
                Registries.DIMENSION,
                buf.readResourceLocation()
        );

        return new ServerBoundUpdateHoleShowcasePacket(pos, config, levelKey);
    }

    public static void handle(ServerBoundUpdateHoleShowcasePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }

            ServerLevel level = sender.server.getLevel(packet.levelKey);

            if (level == null) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (blockEntity instanceof HoleShowcaseBlockEntity holeShowcase) {
                holeShowcase.config = packet.config;
                holeShowcase.setChanged();

                // Optional but usually useful if clients need to see the updated BE data.
                level.sendBlockUpdated(
                        packet.pos,
                        holeShowcase.getBlockState(),
                        holeShowcase.getBlockState(),
                        3
                );
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}