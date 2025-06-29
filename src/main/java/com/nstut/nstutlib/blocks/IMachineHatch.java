package com.nstut.nstutlib.blocks;

import net.minecraft.core.Direction;

public interface IMachineHatch {
    String getHatchType();
    Direction getHatchDirection();
    // You might add more methods here as needed for interaction, e.g.,
    // void transferEnergy(int amount);
    // void transferFluid(FluidStack fluid);
    // void transferItem(ItemStack item);
}