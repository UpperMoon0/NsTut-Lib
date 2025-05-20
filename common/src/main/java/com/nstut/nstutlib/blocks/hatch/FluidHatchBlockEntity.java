package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.transfer.IFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FluidHatchBlockEntity extends HatchBlockEntity implements IFluidStorage {
    public static final long FLUID_CAPACITY_MB = 32000; // 32 buckets

    public FluidHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    // IFluidStorage methods will be implemented in Fabric/Forge specific classes
}
