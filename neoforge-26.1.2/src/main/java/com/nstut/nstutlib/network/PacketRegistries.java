package com.nstut.nstutlib.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketRegistries {
    private static final String PROTOCOL_VERSION = "2";

    private PacketRegistries() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(StructureScannerS2CPacket.TYPE, StructureScannerS2CPacket.STREAM_CODEC, StructureScannerS2CPacket::handle);
        registrar.playToServer(StructureScannerC2SPacket.TYPE, StructureScannerC2SPacket.STREAM_CODEC, StructureScannerC2SPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
