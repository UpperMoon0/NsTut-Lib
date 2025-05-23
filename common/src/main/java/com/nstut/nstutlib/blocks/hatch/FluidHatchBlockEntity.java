package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.transfer.IFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Added imports for Container and related classes
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.nstut.nstutlib.menu.FluidHatchMenu; // Added for createMenu

public abstract class FluidHatchBlockEntity extends HatchBlockEntity implements IFluidStorage, Container { // Implemented Container
    public static final long FLUID_CAPACITY_MB = 32000; // 32 buckets
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public FluidHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    // IFluidStorage methods will be implemented in Fabric/Forge specific classes

    // Container methods for the two item slots
    @Override
    public int getContainerSize() {
        return 2; // Input and Output slots
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
        return super.stillValid(pPlayer);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64; // Or 1 if you only want one container per slot
    }

    // Add createMenu override
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FluidHatchMenu(pContainerId, pPlayerInventory, this, pPlayer);
    }
}
