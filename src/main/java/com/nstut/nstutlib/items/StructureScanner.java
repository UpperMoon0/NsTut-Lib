package com.nstut.nstutlib.items;

import com.nstut.nstutlib.network.PacketRegistries;
import com.nstut.nstutlib.network.StructureScannerS2CPacket;
import com.nstut.nstutlib.views.StructureScannerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class StructureScanner extends Item {
    public StructureScanner(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack scannerItem = player.getItemInHand(hand);

        if (!level.isClientSide) { // Server-side logic
            HitResult hitResult = player.pick(4, 0, false); // Simulates Minecraft's default interaction ray-tracing.

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                int blockX = blockHitResult.getBlockPos().getX();
                int blockY = blockHitResult.getBlockPos().getY();
                int blockZ = blockHitResult.getBlockPos().getZ();

                if (player.isShiftKeyDown()) {
                    // Set second corner
                    scannerItem.getOrCreateTag().putInt("SecondCornerX", blockX);
                    scannerItem.getOrCreateTag().putInt("SecondCornerY", blockY);
                    scannerItem.getOrCreateTag().putInt("SecondCornerZ", blockZ);
                    player.displayClientMessage(Component.literal("Second corner set to: " + blockX + ", " + blockY + ", " + blockZ), true);
                } else {
                    // Set first corner
                    scannerItem.getOrCreateTag().putInt("FirstCornerX", blockX);
                    scannerItem.getOrCreateTag().putInt("FirstCornerY", blockY);
                    scannerItem.getOrCreateTag().putInt("FirstCornerZ", blockZ);
                    player.displayClientMessage(Component.literal("First corner set to: " + blockX + ", " + blockY + ", " + blockZ), true);
                }

                return InteractionResultHolder.sidedSuccess(scannerItem, level.isClientSide());
            }

            // Send the packet to the client to open the GUI
            int firstCornerX = scannerItem.getOrCreateTag().getInt("FirstCornerX");
            int firstCornerY = scannerItem.getOrCreateTag().getInt("FirstCornerY");
            int firstCornerZ = scannerItem.getOrCreateTag().getInt("FirstCornerZ");
            int secondCornerX = scannerItem.getOrCreateTag().getInt("SecondCornerX");
            int secondCornerY = scannerItem.getOrCreateTag().getInt("SecondCornerY");
            int secondCornerZ = scannerItem.getOrCreateTag().getInt("SecondCornerZ");

            if (player instanceof ServerPlayer) {
                PacketRegistries.sendToClients(new StructureScannerS2CPacket(firstCornerX, firstCornerY, firstCornerZ, secondCornerX, secondCornerY, secondCornerZ));
            }
        } else { // Client-side logic
            if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.MISS) {
                Minecraft.getInstance().setScreen(new StructureScannerScreen(level, scannerItem));
                return InteractionResultHolder.sidedSuccess(scannerItem, level.isClientSide());
            }
        }

        return InteractionResultHolder.pass(scannerItem);
    }
}
