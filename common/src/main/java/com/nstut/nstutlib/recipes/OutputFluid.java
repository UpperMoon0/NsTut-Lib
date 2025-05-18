package com.nstut.nstutlib.recipes;

import dev.architectury.fluid.FluidStack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class OutputFluid {
    protected FluidStack fluidStack;
    private float chance;

    public OutputFluid(FluidStack fluidStack, float chance) {
        this.fluidStack = fluidStack;
        this.chance = chance;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
