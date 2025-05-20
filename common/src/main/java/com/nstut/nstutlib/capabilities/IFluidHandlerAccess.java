package com.nstut.nstutlib.capabilities;

import com.nstut.nstutlib.transfer.IFluidStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IFluidHandlerAccess {
    @Nullable
    IFluidStorage getFluidHandler(@Nullable Direction side);
}
