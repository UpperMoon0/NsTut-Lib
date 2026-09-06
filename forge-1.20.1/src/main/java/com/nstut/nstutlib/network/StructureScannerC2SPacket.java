package com.nstut.nstutlib.network;

import com.nstut.nstutlib.items.StructureScanner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StructureScannerC2SPacket {
    private final int firstX;
    private final int firstY;
    private final int firstZ;
    private final int secondX;
    private final int secondY;
    private final int secondZ;

    public StructureScannerC2SPacket(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        this.firstX = firstX;
        this.firstY = firstY;
        this.firstZ = firstZ;
        this.secondX = secondX;
        this.secondY = secondY;
        this.secondZ = secondZ;
    }

    public StructureScannerC2SPacket(FriendlyByteBuf buf) {
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
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack scanner = findScanner(player);
            if (scanner.isEmpty()) {
                return;
            }

            CompoundTag tag = scanner.getOrCreateTag();
            tag.putInt("FirstCornerX", firstX);
            tag.putInt("FirstCornerY", firstY);
            tag.putInt("FirstCornerZ", firstZ);
            tag.putInt("SecondCornerX", secondX);
            tag.putInt("SecondCornerY", secondY);
            tag.putInt("SecondCornerZ", secondZ);
            player.getInventory().setChanged();
            player.displayClientMessage(Component.literal("Structure corners updated"), true);
        });
        context.setPacketHandled(true);
    }

    private static ItemStack findScanner(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof StructureScanner) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof StructureScanner ? offHand : ItemStack.EMPTY;
    }
}
