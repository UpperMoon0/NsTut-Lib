package com.nstut.nstutlib.network;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.client.ClientPacketHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record StructureScannerS2CPacket(int firstX, int firstY, int firstZ,
                                        int secondX, int secondY, int secondZ) implements CustomPacketPayload {
    public static final Type<StructureScannerS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NsTutLib.MOD_ID, "structure_scanner_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureScannerS2CPacket> STREAM_CODEC =
            StreamCodec.ofMember(StructureScannerS2CPacket::write, StructureScannerS2CPacket::new);

    public StructureScannerS2CPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(firstX);
        buf.writeInt(firstY);
        buf.writeInt(firstZ);
        buf.writeInt(secondX);
        buf.writeInt(secondY);
        buf.writeInt(secondZ);
    }

    public void handle(IPayloadContext context) {
        if (FMLLoader.getCurrent().getDist().isClient()) {
            ClientPacketHandlers.openStructureScanner(firstX, firstY, firstZ, secondX, secondY, secondZ);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
