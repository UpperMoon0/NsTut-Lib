package com.nstut.nstutlib.fabric.core.registry;

import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.fabric.blocks.hatch.FabricFluidHatchBlockEntity;
import com.nstut.nstutlib.fabric.blocks.hatch.FabricItemHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NsTutLibBlockEntitiesImpl {
    public static ItemHatchBlockEntity getPlatformSpecificItemHatchBE(BlockPos pos, BlockState state) {
        // Pass the registered BlockEntityType for item hatches
        return new FabricItemHatchBlockEntity(NsTutLibBlockEntities.ITEM_HATCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static FluidHatchBlockEntity getPlatformSpecificFluidHatchBE(BlockPos pos, BlockState state) {
        // Pass the registered BlockEntityType for fluid hatches
        return new FabricFluidHatchBlockEntity(NsTutLibBlockEntities.FLUID_HATCH_BLOCK_ENTITY.get(), pos, state);
    }
}
