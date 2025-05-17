package com.nstut.nstutlib.recipes;

import dev.architectury.fluid.FluidStack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IngredientFluid extends RecipeFluid {
    private boolean isConsumable;

    public IngredientFluid(FluidStack fluidStack, boolean isConsumable) {
        super(fluidStack);
        this.isConsumable = isConsumable;
    }
}
