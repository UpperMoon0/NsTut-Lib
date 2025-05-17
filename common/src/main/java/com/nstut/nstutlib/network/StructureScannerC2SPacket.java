package com.nstut.nstutlib.network;

import com.nstut.nstutlib.items.StructureScanner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import dev.architectury.networking.NetworkManager; // Added
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
// import net.minecraftforge.network.NetworkEvent; // Removed
import net.minecraft.world.entity.player.Player; // Added for context.getPlayer()

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

    // Updated handle method signature
    public void handle(NetworkManager.PacketContext context) {
        // Architectury's NetworkManager#queue ensures this runs on the game thread for the correct side (server for C2S)
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
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
                    serverPlayer.displayClientMessage(Component.literal("Structure corners updated!"), true); // This is fine, sends a message to the client.
                }
            }
        });
        // No setPacketHandled in Architectury
    }
}
