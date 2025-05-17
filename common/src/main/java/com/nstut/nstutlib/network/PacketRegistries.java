package com.nstut.nstutlib.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistries {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("nstutlib", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(StructureScannerS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(StructureScannerS2CPacket::new)
                .encoder(StructureScannerS2CPacket::toBytes)
                .consumerMainThread(StructureScannerS2CPacket::handle)
                .add();

        net.messageBuilder(StructureScannerC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StructureScannerC2SPacket::new)
                .encoder(StructureScannerC2SPacket::toBytes)
                .consumerMainThread(StructureScannerC2SPacket::handle)
                .add();
    }
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }
}
