package com.nstut.nstutlib.network;

import com.nstut.nstutlib.NsTutLib;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PacketRegistries {

    public static final ResourceLocation STRUCTURE_SCANNER_S2C_PACKET_ID = new ResourceLocation(NsTutLib.MOD_ID, "structure_scanner_s2c");
    public static final ResourceLocation STRUCTURE_SCANNER_C2S_PACKET_ID = new ResourceLocation(NsTutLib.MOD_ID, "structure_scanner_c2s");

    public static void register() {
        // Server to Client
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, STRUCTURE_SCANNER_S2C_PACKET_ID,
                (buf, context) -> { // contextSupplier is the context itself
                    StructureScannerS2CPacket packet = new StructureScannerS2CPacket(buf);
                    context.queue(() -> packet.handle(context)); // Use context directly
                });

        // Client to Server
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, STRUCTURE_SCANNER_C2S_PACKET_ID,
                (buf, context) -> { // contextSupplier is the context itself
                    StructureScannerC2SPacket packet = new StructureScannerC2SPacket(buf);
                    context.queue(() -> packet.handle(context)); // Use context directly
                });
    }

    // Generic sendToClients - specific implementation might depend on context (e.g., sending to all players on a server)
    // For now, this method assumes you have a way to get all players or a specific player.
    // If sending to all players on a server:
    public static void sendToAllClients(Object packetInstance) {
        if (packetInstance instanceof StructureScannerS2CPacket pkt) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            pkt.toBytes(buf);
            // This requires a MinecraftServer instance to get all players.
            // For example: MinecraftServer server = contextSupplier.get().getPlayer().getServer();
            // if (server != null) {
            //     NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), STRUCTURE_SCANNER_S2C_PACKET_ID, buf);
            // }
            // As a simpler placeholder if you always send to all connected players:
            // NetworkManager.sendToAllPlayers(STRUCTURE_SCANNER_S2C_PACKET_ID, buf); // This method might not exist directly,
            // you usually iterate server.getPlayerList().getPlayers()
            NsTutLib.getLogger().warn("PacketRegistries.sendToAllClients needs a list of ServerPlayers or a MinecraftServer instance to send packets.");
        }
    }
    
    public static void sendToClient(ServerPlayer player, Object packetInstance) {
        if (packetInstance instanceof StructureScannerS2CPacket pkt) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            pkt.toBytes(buf);
            NetworkManager.sendToPlayer(player, STRUCTURE_SCANNER_S2C_PACKET_ID, buf);
        }
    }

    public static void sendToServer(Object packetInstance) {
        if (packetInstance instanceof StructureScannerC2SPacket pkt) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            pkt.toBytes(buf);
            NetworkManager.sendToServer(STRUCTURE_SCANNER_C2S_PACKET_ID, buf);
        } else {
            NsTutLib.getLogger().error("Attempted to send non-StructureScannerC2SPacket to server: " + packetInstance.getClass().getName());
        }
    }
}
