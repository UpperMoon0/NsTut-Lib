package com.nstut.nstutlib.recipes;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Optional recipe extension for transactions whose exact persisted definition depends on the
 * concrete input state at recipe start. The returned data becomes the authoritative in-flight
 * recipe snapshot and is persisted by the machine transaction engine.
 */
public interface InputAwareRecipeSnapshot {
    ModRecipeData snapshotForExecution(IItemHandler inputSlots,
                                       List<? extends IFluidHandler> inputTanks);
}
