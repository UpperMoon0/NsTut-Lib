package com.nstut.nstutlib.forge.core.registry;

import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.forge.blocks.hatch.ForgeFluidHatchBlockEntity;
import com.nstut.nstutlib.forge.blocks.hatch.ForgeItemHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NsTutLibBlockEntitiesImpl {
    public static ItemHatchBlockEntity getPlatformSpecificItemHatchBE(BlockPos pos, BlockState state) {
        // Pass the registered BlockEntityType for item hatches
        return new ForgeItemHatchBlockEntity(NsTutLibBlockEntities.ITEM_HATCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static FluidHatchBlockEntity getPlatformSpecificFluidHatchBE(BlockPos pos, BlockState state) {
        // Pass the registered BlockEntityType for fluid hatches
        return new ForgeFluidHatchBlockEntity(NsTutLibBlockEntities.FLUID_HATCH_BLOCK_ENTITY.get(), pos, state);
    }
}
