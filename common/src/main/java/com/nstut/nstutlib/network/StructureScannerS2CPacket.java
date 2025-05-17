package com.nstut.nstutlib.network;

import com.nstut.nstutlib.views.StructureScannerScreen;
import dev.architectury.networking.NetworkManager; // Added
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
// import net.minecraftforge.network.NetworkEvent; // Removed

public class StructureScannerS2CPacket {
    private final int firstX, firstY, firstZ;
    private final int secondX, secondY, secondZ;

    public StructureScannerS2CPacket(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        this.firstX = firstX;
        this.firstY = firstY;
        this.firstZ = firstZ;
        this.secondX = secondX;
        this.secondY = secondY;
        this.secondZ = secondZ;
    }

    public StructureScannerS2CPacket(FriendlyByteBuf buf) {
        this.firstX = buf.readInt();
        this.firstY = buf.readInt();
        this.firstZ = buf.readInt();
        this.secondX = buf.readInt();
        this.secondY = buf.readInt();
        this.secondZ = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(firstX);
        buf.writeInt(firstY);
        buf.writeInt(firstZ);
        buf.writeInt(secondX);
        buf.writeInt(secondY);
        buf.writeInt(secondZ);
    }

    // Updated handle method signature to accept PacketContext directly
    public void handle(NetworkManager.PacketContext context) {
        // Architectury's NetworkManager#queue ensures this runs on the game thread for the correct side
        context.queue(() -> {
            // This is an S2C packet, so it's handled on the client.
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().screen instanceof StructureScannerScreen screen) {
                    screen.setCorners(firstX, firstY, firstZ, secondX, secondY, secondZ);
                }
            });
        });
        // No setPacketHandled in Architectury
    }
}
