package com.nstut.nstutlib.recipes;

import dev.architectury.fluid.FluidStack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class IngredientFluid {
    protected FluidStack fluidStack;
    private boolean isConsumable;

    public IngredientFluid(FluidStack fluidStack, boolean isConsumable) {
        this.fluidStack = fluidStack;
        this.isConsumable = isConsumable;
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }
}
