package com.nstut.nstutlib.forge.transfer;

import com.nstut.nstutlib.transfer.IItemStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ForgeItemStorage implements IItemStorage {
    private final IItemHandler itemHandler;
    private final ForgeTransactionContext transactionContext; // Optional, if operations need to be aware of it

    public ForgeItemStorage(IItemHandler itemHandler, ForgeTransactionContext transactionContext) {
        this.itemHandler = itemHandler;
        this.transactionContext = transactionContext;
    }

    // Constructor without explicit transaction context if not always needed at this level
    public ForgeItemStorage(IItemHandler itemHandler) {
        this(itemHandler, null);
    }

    private boolean shouldSimulate(boolean simulate) {
        return transactionContext != null ? transactionContext.shouldSimulate(simulate) : simulate;
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // IItemHandler.insertItem handles iterating through slots to find a place.
        // The simulate flag is passed directly.
        return itemHandler.insertItem(-1, stack, shouldSimulate(simulate)); // -1 for any slot, though typically 0 is used for first available
    }

    @Override
    public ItemStack extract(int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        // Iterate through slots to find an item to extract
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack extracted = itemHandler.extractItem(i, amount, shouldSimulate(simulate));
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot < 0 || slot >= itemHandler.getSlots()) return ItemStack.EMPTY;
        return itemHandler.extractItem(slot, amount, shouldSimulate(simulate));
    }

    @Override
    public int getSlotCount() {
        return itemHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= itemHandler.getSlots()) return ItemStack.EMPTY;
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= itemHandler.getSlots()) return 0;
        return itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot < 0 || slot >= itemHandler.getSlots() || stack.isEmpty()) return false; // Or true for empty stack based on convention
        return itemHandler.isItemValid(slot, stack);
    }
}
