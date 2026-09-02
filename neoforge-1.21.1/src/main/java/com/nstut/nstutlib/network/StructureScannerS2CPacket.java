package com.nstut.nstutlib.network;

import com.nstut.nstutlib.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StructureScannerS2CPacket {
    private final int firstX;
    private final int firstY;
    private final int firstZ;
    private final int secondX;
    private final int secondY;
    private final int secondZ;

    public StructureScannerS2CPacket(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        this.firstX = firstX;
        this.firstY = firstY;
        this.firstZ = firstZ;
        this.secondX = secondX;
        this.secondY = secondY;
        this.secondZ = secondZ;
    }

    public StructureScannerS2CPacket(FriendlyByteBuf buf) {
        firstX = buf.readInt();
        firstY = buf.readInt();
        firstZ = buf.readInt();
        secondX = buf.readInt();
        secondY = buf.readInt();
        secondZ = buf.readInt();
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
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientPacketHandlers.openStructureScanner(
                        firstX, firstY, firstZ, secondX, secondY, secondZ)));
        context.setPacketHandled(true);
    }
}
