package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class OutputEnergyHatchBlock extends EnergyHatchBlock {
    public OutputEnergyHatchBlock(Properties pProperties, EnergyTier tier) {
        super(pProperties, tier);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return NsTutLibBlockEntities.OUTPUT_ENERGY_HATCH_BLOCK_ENTITY.get().create(pPos, pState);
    }
}
