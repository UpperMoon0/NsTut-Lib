package com.nstut.nstutlib.recipes;

import dev.architectury.fluid.FluidStack;

public class RecipeFluid {
    protected final FluidStack fluidStack;

    public RecipeFluid(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }

    public long getAmount() {
        return fluidStack.getAmount();
    }
}
