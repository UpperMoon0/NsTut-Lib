package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class EnergyHatchBlock extends HatchBlock {
    protected final EnergyTier tier;

    protected EnergyHatchBlock(Properties pProperties, EnergyTier tier) {
        super(pProperties);
        this.tier = tier;
    }

    public EnergyTier getTier() {
        return tier;
    }

    // This will be implemented by InputEnergyHatchBlock and OutputEnergyHatchBlock
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState);
}
