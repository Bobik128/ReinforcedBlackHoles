package com.mod.rbh.network;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.network.packet.ClientboundShootPacket;
import com.mod.rbh.network.packet.ServerboundFirearmActionPacket;
import com.mod.rbh.network.packet.ServerboundSetAttackKeyPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

public class RBHNetwork {

    private static SimpleChannel NETWORK = construct();
    public static final String VERSION = "1.0.0";

    private static SimpleChannel construct() {
        SimpleChannel network = NetworkRegistry.ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID,"network"))
                .clientAcceptedVersions(VERSION::equals)
                .serverAcceptedVersions(VERSION::equals)
                .networkProtocolVersion(() -> VERSION)
                .simpleChannel();

        int id = 0;

        buildMessage(network, id++, ServerboundSetAttackKeyPacket.class, ServerboundSetAttackKeyPacket::new);
        buildMessage(network, id++, ServerboundFirearmActionPacket.class, ServerboundFirearmActionPacket::new);
        buildMessage(network, id++, ClientboundShootPacket.class, ClientboundShootPacket::new);

        return network;
    }

    private static <MSG extends RBHPacket> void buildMessage(SimpleChannel network, int id, Class<MSG> msg, Function<FriendlyByteBuf, MSG> decoder) {
        network.messageBuilder(msg, id)
                .decoder(decoder)
                .encoder(RBHPacket::rootEncode)
                .consumerMainThread(RBHNetwork::consumeRBHPacket)
                .add();
    }

    private static <MSG extends RBHPacket> void consumeRBHPacket(MSG packet, Supplier<NetworkEvent.Context> sup) {
        NetworkEvent.Context ctx = sup.get();
        packet.handle(ctx::enqueueWork, ctx.getNetworkManager().getPacketListener(), ctx.getSender());
        ctx.setPacketHandled(true);
    }

    public static <MSG extends RBHPacket> void sendToServer(MSG msg) { NETWORK.sendToServer(msg); }

    public static <MSG extends RBHPacket> void sendToPlayer(MSG msg, ServerPlayer player) {
        NETWORK.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <MSG extends RBHPacket> void sendToAll(MSG msg) {
        NETWORK.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static <MSG extends RBHPacket> void sendToAllInDimension(MSG msg, Level level) {
        NETWORK.send(PacketDistributor.DIMENSION.with(level::dimension), msg);
    }

    public static void init() {}

}