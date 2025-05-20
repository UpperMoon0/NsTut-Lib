package com.nstut.nstutlib.capabilities;

import com.nstut.nstutlib.transfer.IItemStorage; 
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IItemHandlerAccess {
    @Nullable
    IItemStorage getItemHandler(@Nullable Direction side); 
}
