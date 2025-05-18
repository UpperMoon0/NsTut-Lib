package com.nstut.nstutlib.fabric.transfer;

import com.nstut.nstutlib.transfer.IItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;

public class FabricItemStorage implements IItemStorage {
    private final Storage<ItemVariant> storage;

    public FabricItemStorage(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemVariant itemVariant = ItemVariant.of(stack);
        long amount = stack.getCount();

        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            long inserted = storage.insert(itemVariant, amount, ftx.transaction);
            if (simulate) {
                ftx.abort();
            } else {
                ftx.commit();
            }

            if (inserted == amount) {
                return ItemStack.EMPTY;
            } else {
                ItemStack remaining = stack.copy();
                remaining.setCount((int) (amount - inserted));
                return remaining;
            }
        }
    }

    @Override
    public ItemStack extract(int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;

        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            for (StorageView<ItemVariant> view : storage) { // Iterate directly
                if (!view.isResourceBlank()) {
                    ItemVariant resource = view.getResource();
                    // Pass ftx.transaction to the extract call
                    long extractedAmount = storage.extract(resource, amount, ftx.transaction);
                    if (extractedAmount > 0) {
                        if (simulate) {
                            ftx.abort();
                        } else {
                            ftx.commit();
                        }
                        return resource.toStack((int) extractedAmount);
                    }
                }
            }
            // If loop completes, no item was extracted or storage is empty.
            // The transaction should be handled based on whether it was a simulation or not.
            // If it's not a simulation and nothing happened, it's effectively a commit of no change.
            // If it is a simulation, it should be aborted.
            if (simulate) {
                ftx.abort();
            } else {
                ftx.commit(); // Or abort() if preferred when no action is taken.
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        int currentSlot = 0;

        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            for (StorageView<ItemVariant> view : storage) { // Iterate directly
                // We need to count all views to correctly identify the slot index,
                // then check if the view at that slot is blank.
                if (currentSlot == slot) {
                    if (view.isResourceBlank()) {
                        if (simulate) ftx.abort(); else ftx.commit();
                        return ItemStack.EMPTY; // Slot is empty
                    }
                    ItemVariant resource = view.getResource();
                    // Pass ftx.transaction to the extract call
                    long extractedAmount = storage.extract(resource, amount, ftx.transaction);
                    if (extractedAmount > 0) {
                        if (simulate) {
                            ftx.abort();
                        } else {
                            ftx.commit();
                        }
                        return resource.toStack((int) extractedAmount);
                    }
                    // Extraction failed from the target slot (e.g. not enough items)
                    if (simulate) ftx.abort(); else ftx.commit(); // Or abort()
                    return ItemStack.EMPTY;
                }
                currentSlot++;
            }
            // Slot index out of bounds
            if (simulate) ftx.abort(); else ftx.commit(); // Or abort()
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotCount() {
        int count = 0;
        // Read-only operations still need a transaction context for API consistency
        // and in case the underlying storage does something with it.
        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            for (StorageView<ItemVariant> view : storage) { // Iterate directly
                count++;
            }
            ftx.commit(); // Read-only operation, commit is appropriate.
        }
        return count;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int currentSlot = 0;
        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            for (StorageView<ItemVariant> view : storage) { // Iterate directly
                if (currentSlot == slot) {
                    ftx.commit(); // Read-only
                    if (view.isResourceBlank()) return ItemStack.EMPTY;
                    return view.getResource().toStack((int) view.getAmount());
                }
                currentSlot++;
            }
            ftx.commit(); // Read-only, slot not found
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        int currentSlot = 0;
        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            for (StorageView<ItemVariant> view : storage) { // Iterate directly
                if (currentSlot == slot) {
                    ftx.commit(); // Read-only
                    // view.getCapacity() gives the max amount of the current resource the view can hold.
                    // For a general slot limit, this might be ItemVariant.MAX_STACK_SIZE or specific to the inventory.
                    // The Fabric API is more about current contents and transfer capacity than fixed slot limits.
                    // We'll return the view's capacity as a best effort.
                    return (int) view.getCapacity();
                }
                currentSlot++;
            }
            ftx.commit(); // Read-only, slot not found
        }
        return 0; // Default if slot not found
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        // Fabric's Storage API (insert/extract) doesn't directly support slot-specific validation checks
        // in the same way as Forge's IItemHandler#isItemValid(slot, stack).
        // The `storage.insert` method attempts to insert into any available space.
        // To check for a *specific* slot, one would typically need access to the underlying inventory's logic,
        // which Storage<T> abstracts away.
        // This implementation will check if the item can be inserted anywhere in the storage, which is a general validity.
        if (stack.isEmpty()) {
            return true; // Or false, depending on how empty stacks should be treated for validity.
        }
        ItemVariant itemVariant = ItemVariant.of(stack);
        try (FabricTransactionContext ftx = FabricTransactionContext.open()) {
            // Simulate an insert of 1 item to see if it's accepted by the storage anywhere.
            long inserted = storage.insert(itemVariant, 1, ftx.transaction);
            ftx.abort(); // Always abort, this is a check.
            return inserted > 0;
        }
    }
}
