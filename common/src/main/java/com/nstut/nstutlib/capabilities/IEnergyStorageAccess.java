package com.nstut.nstutlib.capabilities;

// Assuming you'll create a corresponding IEnergyStorage in the transfer package
import com.nstut.nstutlib.transfer.IEnergyStorage; 
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IEnergyStorageAccess {
    @Nullable
    IEnergyStorage getEnergyStorage(@Nullable Direction side);
}
