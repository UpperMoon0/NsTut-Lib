package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EnergyHatchBlockEntity extends HatchBlockEntity {
    protected final EnergyTier tier;

    public EnergyHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, EnergyTier tier) {
        super(pType, pPos, pBlockState);
        this.tier = tier;
    }

    public EnergyTier getTier() {
        return tier;
    }

    public abstract long getEnergyStored();
    public abstract long getMaxEnergyStored();
    public abstract boolean canExtract();
    public abstract boolean canReceive();
    public abstract long receiveEnergy(long maxReceive, boolean simulate);
    public abstract long extractEnergy(long maxExtract, boolean simulate);
}
