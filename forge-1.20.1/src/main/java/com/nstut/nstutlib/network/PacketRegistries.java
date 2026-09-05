package com.nstut.nstutlib.network;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketRegistries {
    private static final String PROTOCOL_VERSION = "2";
    private static SimpleChannel instance;
    private static int packetId;

    private PacketRegistries() {
    }

    private static int id() {
        return packetId++;
    }

    public static void register() {
        packetId = 0;
        instance = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(NsTutLib.MOD_ID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        instance.messageBuilder(StructureScannerS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(StructureScannerS2CPacket::new)
                .encoder(StructureScannerS2CPacket::toBytes)
                .consumerMainThread(StructureScannerS2CPacket::handle)
                .add();

        instance.messageBuilder(StructureScannerC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StructureScannerC2SPacket::new)
                .encoder(StructureScannerC2SPacket::toBytes)
                .consumerMainThread(StructureScannerC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        instance.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer(MSG message) {
        instance.send(PacketDistributor.SERVER.noArg(), message);
    }
}
