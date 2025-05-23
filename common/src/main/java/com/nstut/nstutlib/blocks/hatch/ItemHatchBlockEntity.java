package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.transfer.IItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class ItemHatchBlockEntity extends HatchBlockEntity implements IItemStorage, Container { // Implemented Container
    public static final int ITEM_SLOT_COUNT = 9;
    protected NonNullList<ItemStack> items = NonNullList.withSize(ITEM_SLOT_COUNT, ItemStack.EMPTY);

    public ItemHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    // Methods to be implemented by platform-specific classes if not covered by IItemStorage
    public abstract void setStackInSlot(int slot, @NotNull ItemStack stack);
    public abstract NonNullList<ItemStack> getItems();

    // IItemStorage methods will be implemented in Fabric/Forge specific classes

    // Container methods for vanilla inventory interaction
    @Override
    public int getContainerSize() {
        return ITEM_SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int pSlot) {
        return this.items.get(pSlot);
    }

    @Override
    public @NotNull ItemStack removeItem(int pSlot, int pAmount) {
        return ContainerHelper.removeItem(this.items, pSlot, pAmount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int pSlot) {
        return ContainerHelper.takeItem(this.items, pSlot);
    }

    @Override
    public void setItem(int pSlot, @NotNull ItemStack pStack) {
        this.items.set(pSlot, pStack);
        if (pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return super.stillValid(pPlayer); // Uses the implementation from HatchBlockEntity
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    // Potentially override in platform specific if needed, or if IItemStorage provides it
    @Override
    public int getMaxStackSize() {
        return 64; // Default Minecraft max stack size
    }
}
