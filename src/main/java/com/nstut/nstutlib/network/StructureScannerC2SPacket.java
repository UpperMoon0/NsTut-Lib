package com.nstut.nstutlib.network;

import com.nstut.nstutlib.items.StructureScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StructureScannerC2SPacket {
    private final int firstX, firstY, firstZ;
    private final int secondX, secondY, secondZ;

    public StructureScannerC2SPacket(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        this.firstX = firstX;
        this.firstY = firstY;
        this.firstZ = firstZ;
        this.secondX = secondX;
        this.secondY = secondY;
        this.secondZ = secondZ;
    }

    public StructureScannerC2SPacket(FriendlyByteBuf buf) {
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
            // Ensure we are running on the server side
            if (context.getSender() != null && context.getSender() instanceof ServerPlayer) {
                ServerPlayer serverPlayer = context.getSender();

                // The rest of your logic here
                ItemStack scannerItem = serverPlayer.getMainHandItem();
                if (scannerItem.getItem() instanceof StructureScanner) {
                    CompoundTag tag = scannerItem.getOrCreateTag();
                    tag.putInt("FirstCornerX", this.firstX);
                    tag.putInt("FirstCornerY", this.firstY);
                    tag.putInt("FirstCornerZ", this.firstZ);
                    tag.putInt("SecondCornerX", this.secondX);
                    tag.putInt("SecondCornerY", this.secondY);
                    tag.putInt("SecondCornerZ", this.secondZ);

                    // Mark the item as changed and update the inventory
                    scannerItem.setTag(tag);

                    // Optionally, send feedback to the player
                    serverPlayer.displayClientMessage(Component.literal("Structure corners updated!"), true);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
