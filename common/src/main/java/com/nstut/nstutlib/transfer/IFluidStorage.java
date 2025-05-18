package com.nstut.nstutlib.transfer;

import dev.architectury.fluid.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for custom fluid storage.
 */
public interface IFluidStorage extends ITransferStorage {
    /**
     * Inserts a FluidStack into the storage.
     *
     * @param stack The FluidStack to insert.
     * @param simulate If true, the insertion is only simulated.
     * @return The amount of fluid that was inserted.
     */
    long insert(@NotNull FluidStack stack, boolean simulate);

    /**
     * Extracts a FluidStack from the storage.
     *
     * @param amount The maximum amount to extract.
     * @param simulate If true, the extraction is only simulated.
     * @return The extracted FluidStack.
     */
    @NotNull
    FluidStack extract(long amount, boolean simulate);

    /**
     * Gets the number of tanks in the storage.
     *
     * @return The number of tanks.
     */
    int getTankCount();

    /**
     * Gets the FluidStack in a specific tank.
     *
     * @param tank The tank index.
     * @return The FluidStack in the tank.
     */
    @NotNull
    FluidStack getFluidInTank(int tank);

    /**
     * Gets the capacity of a specific tank.
     *
     * @param tank The tank index.
     * @return The capacity of the tank.
     */
    long getTankCapacity(int tank);

    /**
     * Checks if a FluidStack is valid for a specific tank.
     *
     * @param tank The tank index.
     * @param stack The FluidStack to check.
     * @return True if the FluidStack is valid for the tank, false otherwise.
     */
    boolean isFluidValid(int tank, @NotNull FluidStack stack);
}
