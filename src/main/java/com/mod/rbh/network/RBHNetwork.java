package com.mod.rbh.network;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.network.packet.ClientBoundOpenGuiPacket;
import com.mod.rbh.network.packet.ServerBoundUpdateHoleShowcasePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RBHNetwork {
    public static final String VERSION = "1.0.0";

    public static void register(IEventBus modBus) {
        modBus.addListener(RBHNetwork::registerPayloads);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ReinforcedBlackHoles.MODID)
                .versioned(VERSION);

        registrar.playToClient(
                ClientBoundOpenGuiPacket.TYPE,
                ClientBoundOpenGuiPacket.STREAM_CODEC,
                ClientBoundOpenGuiPacket::handle
        );

        registrar.playToServer(
                ServerBoundUpdateHoleShowcasePacket.TYPE,
                ServerBoundUpdateHoleShowcasePacket.STREAM_CODEC,
                ServerBoundUpdateHoleShowcasePacket::handle
        );
    }

    public static void sendToServer(ServerBoundUpdateHoleShowcasePacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(ClientBoundOpenGuiPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAll(ClientBoundOpenGuiPacket packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendToAllInDimension(ClientBoundOpenGuiPacket packet, ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, packet);
    }
}