package com.nstut.nstutlib.recipes;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Optional recipe extension for transactions whose exact persisted definition depends on the
 * concrete item state present at recipe start. The engine supplies immutable stack copies; the
 * returned data becomes the authoritative in-flight recipe snapshot and is persisted across reloads.
 */
public interface InputAwareRecipeSnapshot {
    ModRecipeData snapshotForExecution(List<ItemStack> itemInputs);
}
