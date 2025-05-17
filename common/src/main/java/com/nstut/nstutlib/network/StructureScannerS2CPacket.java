package com.nstut.nstutlib.network;

import com.nstut.nstutlib.views.StructureScannerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Run on the client to update the UI
            if (context.getDirection().getReceptionSide().isClient()) {
                Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof StructureScannerScreen screen) {
                        screen.setCorners(firstX, firstY, firstZ, secondX, secondY, secondZ);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
