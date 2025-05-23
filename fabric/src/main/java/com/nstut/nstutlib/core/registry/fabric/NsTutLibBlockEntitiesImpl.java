package com.nstut.nstutlib.core.registry.fabric;

import com.nstut.nstutlib.blocks.hatch.EnergyHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.fabric.blocks.hatch.FabricEnergyHatchBlockEntity;
import com.nstut.nstutlib.fabric.blocks.hatch.FabricFluidHatchBlockEntity;
import com.nstut.nstutlib.fabric.blocks.hatch.FabricItemHatchBlockEntity;
import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NsTutLibBlockEntitiesImpl {
    public static ItemHatchBlockEntity createItemHatchBE(BlockPos pos, BlockState state) {
        return new FabricItemHatchBlockEntity(NsTutLibBlockEntities.ITEM_HATCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static FluidHatchBlockEntity createFluidHatchBE(BlockPos pos, BlockState state) {
        return new FabricFluidHatchBlockEntity(NsTutLibBlockEntities.FLUID_HATCH_BLOCK_ENTITY.get(), pos, state);
    }

    public static EnergyHatchBlockEntity getPlatformSpecificInputEnergyHatchBE(BlockPos pos, BlockState state, EnergyTier tier) {
        return new FabricEnergyHatchBlockEntity(NsTutLibBlockEntities.INPUT_ENERGY_HATCH_BLOCK_ENTITY.get(), pos, state, tier, true);
    }

    public static EnergyHatchBlockEntity getPlatformSpecificOutputEnergyHatchBE(BlockPos pos, BlockState state, EnergyTier tier) {
        return new FabricEnergyHatchBlockEntity(NsTutLibBlockEntities.OUTPUT_ENERGY_HATCH_BLOCK_ENTITY.get(), pos, state, tier, false);
    }
}
