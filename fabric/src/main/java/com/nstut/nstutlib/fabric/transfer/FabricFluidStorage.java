package com.nstut.nstutlib.fabric.transfer;

import com.nstut.nstutlib.transfer.IFluidStorage;
import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class FabricFluidStorage implements IFluidStorage {
    private final Storage<FluidVariant> storage;

    public FabricFluidStorage(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    @Override
    public long insert(FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return 0;
        }

        FluidVariant fluidVariant = FluidVariant.of(stack.getFluid(), stack.getTag());
        long amount = stack.getAmount();

        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            long inserted = storage.insert(fluidVariant, amount, ctx.transaction); // Access package-private field
            if (simulate) {
                ctx.abort();
            } else {
                ctx.commit();
            }
            return inserted;
        }
    }

    @Override
    public FluidStack extract(long amount, boolean simulate) {
        if (amount <= 0) return FluidStack.empty();

        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            for (StorageView<FluidVariant> view : storage) { // Direct iteration
                if (!view.isResourceBlank()) {
                    FluidVariant resource = view.getResource();
                    // Ensure the extract operation uses the transaction from the context
                    long extractedAmount = storage.extract(resource, amount, ctx.transaction);
                    if (extractedAmount > 0) {
                        if (simulate) {
                            ctx.abort();
                        } else {
                            ctx.commit();
                        }
                        return FluidStack.create(resource.getFluid(), extractedAmount, resource.getNbt());
                    }
                }
            }
            if (simulate) {
                ctx.abort();
            } else {
                ctx.commit();
            }
            return FluidStack.empty();
        }
    }

    @Override
    public int getTankCount() {
        int count = 0;
        // For read-only operations, we still need a transaction context for iteration if storage requires it.
        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            for (StorageView<FluidVariant> view : storage) { // Direct iteration
                // The iteration itself might require an active transaction, provided by ctx.transaction
                if (!view.isResourceBlank()) {
                    count++;
                }
            }
            ctx.commit(); // Commit the read-only transaction
        }
        return count;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        int currentTank = 0;
        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            for (StorageView<FluidVariant> view : storage) { // Direct iteration
                if (!view.isResourceBlank()) {
                    if (currentTank == tank) {
                        FluidVariant resource = view.getResource();
                        // No modification, just commit the read transaction
                        ctx.commit();
                        return FluidStack.create(resource.getFluid(), view.getAmount(), resource.getNbt());
                    }
                    currentTank++;
                }
            }
            ctx.commit(); // Commit if tank not found (read-only)
        }
        return FluidStack.empty();
    }

    @Override
    public long getTankCapacity(int tank) {
        int currentTank = 0;
        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            for (StorageView<FluidVariant> view : storage) { // Direct iteration
                if (!view.isResourceBlank()) {
                    if (currentTank == tank) {
                        // No modification, just commit the read transaction
                        ctx.commit();
                        return view.getCapacity();
                    }
                    currentTank++;
                }
            }
            ctx.commit(); // Commit if tank not found (read-only)
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        FluidVariant fluidVariant = FluidVariant.of(stack.getFluid(), stack.getTag());
        try (FabricTransactionContext ctx = FabricTransactionContext.open()) {
            // storage.insert will use ctx.transaction
            long inserted = storage.insert(fluidVariant, FluidStack.bucketAmount(), ctx.transaction);
            ctx.abort(); // This is a check, so always abort.
            return inserted > 0;
        }
    }
}
