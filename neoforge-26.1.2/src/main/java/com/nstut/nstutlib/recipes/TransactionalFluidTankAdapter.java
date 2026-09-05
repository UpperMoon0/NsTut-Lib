package com.nstut.nstutlib.recipes;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Adapts a single-tank {@link IFluidHandler} to the restorable {@link FluidTank}
 * contract used by recipe transactions. The delegate remains authoritative for
 * normal I/O while rollback restores the underlying native storage through the
 * supplied callback.
 */
public final class TransactionalFluidTankAdapter extends FluidTank {
    private final IFluidHandler delegate;
    private final Consumer<FluidStack> restorer;

    public TransactionalFluidTankAdapter(IFluidHandler delegate, int capacity, Consumer<FluidStack> restorer) {
        super(capacity);
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.restorer = Objects.requireNonNull(restorer, "restorer");
        if (delegate.getTanks() != 1) {
            throw new IllegalArgumentException("TransactionalFluidTankAdapter requires exactly one delegate tank");
        }
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return delegate.getFluidInTank(0);
    }

    @Override
    public void setFluid(FluidStack stack) {
        restorer.accept(stack.copy());
    }

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        return delegate.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        return delegate.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return delegate.drain(maxDrain, action);
    }
}
