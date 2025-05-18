package com.nstut.nstutlib.transfer;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for custom item storage.
 */
public interface IItemStorage extends ITransferStorage {
    /**
     * Inserts an ItemStack into the storage.
     *
     * @param stack    The ItemStack to insert.
     * @param simulate If true, the insertion is only simulated.
     * @return The remainder of the ItemStack that was not inserted.
     */
    @NotNull
    ItemStack insert(@NotNull ItemStack stack, boolean simulate);

    /**
     * Extracts an ItemStack from the storage.
     *
     * @param amount   The maximum amount to extract.
     * @param simulate If true, the extraction is only simulated.
     * @return The extracted ItemStack.
     */
    @NotNull
    ItemStack extract(int amount, boolean simulate);

    /**
     * Extracts an ItemStack from a specific slot in the storage.
     *
     * @param slot The slot to extract from.
     * @param amount The maximum amount to extract.
     * @param simulate If true, the extraction is only simulated.
     * @return The extracted ItemStack.
     */
    @NotNull
    ItemStack extract(int slot, int amount, boolean simulate);

    /**
     * Gets the number of slots in the storage.
     *
     * @return The number of slots.
     */
    int getSlotCount();

    /**
     * Gets the ItemStack in a specific slot.
     *
     * @param slot The slot index.
     * @return The ItemStack in the slot.
     */
    @NotNull
    ItemStack getStackInSlot(int slot);

    /**
     * Gets the maximum stack size for a specific slot.
     *
     * @param slot The slot index.
     * @return The maximum stack size.
     */
    int getSlotLimit(int slot);

    /**
     * Checks if an ItemStack is valid for a specific slot.
     *
     * @param slot The slot index.
     * @param stack The ItemStack to check.
     * @return True if the ItemStack is valid for the slot, false otherwise.
     */
    boolean isItemValid(int slot, @NotNull ItemStack stack);
}
