package com.nstut.nstutlib.network;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.items.StructureScanner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record StructureScannerC2SPacket(int firstX, int firstY, int firstZ,
                                        int secondX, int secondY, int secondZ) implements CustomPacketPayload {
    public static final Type<StructureScannerC2SPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NsTutLib.MOD_ID, "structure_scanner_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureScannerC2SPacket> STREAM_CODEC =
            StreamCodec.ofMember(StructureScannerC2SPacket::write, StructureScannerC2SPacket::new);

    public StructureScannerC2SPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(firstX); buf.writeInt(firstY); buf.writeInt(firstZ);
        buf.writeInt(secondX); buf.writeInt(secondY); buf.writeInt(secondZ);
    }

    public void handle(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        ItemStack scanner = findScanner(player);
        if (scanner.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, scanner, tag -> {
            tag.putInt("FirstCornerX", firstX); tag.putInt("FirstCornerY", firstY); tag.putInt("FirstCornerZ", firstZ);
            tag.putInt("SecondCornerX", secondX); tag.putInt("SecondCornerY", secondY); tag.putInt("SecondCornerZ", secondZ);
        });
        player.getInventory().setChanged();
        player.sendSystemMessage(Component.literal("Structure corners updated"));
    }

    private static ItemStack findScanner(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof StructureScanner) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof StructureScanner ? offHand : ItemStack.EMPTY;
    }

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
