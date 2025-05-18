package com.nstut.nstutlib.forge.transfer;

import com.nstut.nstutlib.transfer.IFluidStorage;
import dev.architectury.fluid.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ForgeFluidStorage implements IFluidStorage {
    private final IFluidHandler fluidHandler;
    private final ForgeTransactionContext transactionContext;

    public ForgeFluidStorage(IFluidHandler fluidHandler, ForgeTransactionContext transactionContext) {
        this.fluidHandler = fluidHandler;
        this.transactionContext = transactionContext;
    }

    private net.minecraftforge.fluids.FluidStack toForgeStack(FluidStack stack) {
        if (stack.isEmpty()) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        int amount = (int) Math.min(stack.getAmount(), Integer.MAX_VALUE);
        if (amount == 0 && stack.getAmount() > 0) {
            return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), amount, stack.getTag());
        }
        return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), amount, stack.getTag());
    }

    private FluidStack fromForgeStack(net.minecraftforge.fluids.FluidStack forgeStack) {
        if (forgeStack.isEmpty()) {
            return FluidStack.empty();
        }
        return FluidStack.create(forgeStack.getFluid(), forgeStack.getAmount(), forgeStack.getTag());
    }

    @Override
    public long insert(FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return 0;
        }
        net.minecraftforge.fluids.FluidStack forgeStack = toForgeStack(stack);
        if (forgeStack.isEmpty()) return 0;

        return fluidHandler.fill(forgeStack, transactionContext.shouldSimulate(simulate) ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public FluidStack extract(long amount, boolean simulate) {
        if (amount <= 0) {
            return FluidStack.empty();
        }
        int amountToInt = (int) Math.min(amount, Integer.MAX_VALUE);
        if (amountToInt == 0 && amount > 0) {
            return FluidStack.empty();
        }
        if (amountToInt == 0) return FluidStack.empty();


        net.minecraftforge.fluids.FluidStack extracted = fluidHandler.drain(amountToInt, transactionContext.shouldSimulate(simulate) ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        return fromForgeStack(extracted);
    }
    
    @Override
    public int getTankCount() {
        return fluidHandler.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        net.minecraftforge.fluids.FluidStack forgeStack = fluidHandler.getFluidInTank(tank);
        return fromForgeStack(forgeStack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        net.minecraftforge.fluids.FluidStack forgeStack = toForgeStack(stack);
        return fluidHandler.isFluidValid(tank, forgeStack);
    }
}
