package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.transfer.IItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class ItemHatchBlockEntity extends HatchBlockEntity implements IItemStorage {
    public static final int ITEM_SLOT_COUNT = 9;

    public ItemHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    // Methods to be implemented by platform-specific classes if not covered by IItemStorage
    public abstract void setStackInSlot(int slot, @NotNull ItemStack stack);
    public abstract NonNullList<ItemStack> getItems();

    // IItemStorage methods will be implemented in Fabric/Forge specific classes
}
