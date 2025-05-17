package com.nstut.nstutlib.recipes;

import dev.architectury.fluid.FluidStack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutputFluid extends RecipeFluid {
    private float chance;

    public OutputFluid(FluidStack fluidStack, float chance) {
        super(fluidStack);
        this.chance = chance;
    }
}
