package com.nstut.nstutlib.items;

import com.nstut.nstutlib.network.PacketRegistries;
import com.nstut.nstutlib.network.StructureScannerS2CPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
        ItemStack scanner = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(scanner, true);
        }

        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            if (player.isShiftKeyDown()) {
                setCorner(scanner, "SecondCorner", blockHit);
                player.displayClientMessage(Component.literal("Second corner set to " + blockHit.getBlockPos().toShortString()), true);
            } else {
                setCorner(scanner, "FirstCorner", blockHit);
                player.displayClientMessage(Component.literal("First corner set to " + blockHit.getBlockPos().toShortString()), true);
            }
            return InteractionResultHolder.consume(scanner);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = scanner.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            PacketRegistries.sendToPlayer(serverPlayer, new StructureScannerS2CPacket(
                    tag.getInt("FirstCornerX"),
                    tag.getInt("FirstCornerY"),
                    tag.getInt("FirstCornerZ"),
                    tag.getInt("SecondCornerX"),
                    tag.getInt("SecondCornerY"),
                    tag.getInt("SecondCornerZ")));
        }
        return InteractionResultHolder.consume(scanner);
    }

    private static void setCorner(ItemStack scanner, String prefix, BlockHitResult hit) {
        CustomData.update(DataComponents.CUSTOM_DATA, scanner, tag -> {
            tag.putInt(prefix + "X", hit.getBlockPos().getX());
            tag.putInt(prefix + "Y", hit.getBlockPos().getY());
            tag.putInt(prefix + "Z", hit.getBlockPos().getZ());
        });
    }
}
