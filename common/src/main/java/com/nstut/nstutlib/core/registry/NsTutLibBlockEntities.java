package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.blocks.hatch.EnergyHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.InputEnergyHatchBlock;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.OutputEnergyHatchBlock;
import com.nstut.nstutlib.util.EnergyTier;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NsTutLibBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(NsTutLib.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<ItemHatchBlockEntity>> ITEM_HATCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_hatch_block_entity",
            () -> BlockEntityType.Builder.of(NsTutLibBlockEntities::createItemHatchBE,
                    NsTutLibBlocks.INPUT_ITEM_HATCH_BLOCK.get(),
                    NsTutLibBlocks.OUTPUT_ITEM_HATCH_BLOCK.get()
            ).build(null));

    public static final RegistrySupplier<BlockEntityType<FluidHatchBlockEntity>> FLUID_HATCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("fluid_hatch_block_entity",
            () -> BlockEntityType.Builder.of(NsTutLibBlockEntities::createFluidHatchBE,
                    NsTutLibBlocks.INPUT_FLUID_HATCH_BLOCK.get(),
                    NsTutLibBlocks.OUTPUT_FLUID_HATCH_BLOCK.get()
            ).build(null));

    public static final RegistrySupplier<BlockEntityType<EnergyHatchBlockEntity>> INPUT_ENERGY_HATCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("input_energy_hatch_block_entity",
            () -> BlockEntityType.Builder.of((p,s) -> getPlatformSpecificInputEnergyHatchBE(p,s, ((InputEnergyHatchBlock)s.getBlock()).getTier()),
                    NsTutLibBlocks.INPUT_ENERGY_HATCH_TIER_1_BLOCK.get(),
                    NsTutLibBlocks.INPUT_ENERGY_HATCH_TIER_2_BLOCK.get(),
                    NsTutLibBlocks.INPUT_ENERGY_HATCH_TIER_3_BLOCK.get(),
                    NsTutLibBlocks.INPUT_ENERGY_HATCH_TIER_4_BLOCK.get()
            ).build(null));

    public static final RegistrySupplier<BlockEntityType<EnergyHatchBlockEntity>> OUTPUT_ENERGY_HATCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("output_energy_hatch_block_entity",
            () -> BlockEntityType.Builder.of((p,s) -> getPlatformSpecificOutputEnergyHatchBE(p,s, ((OutputEnergyHatchBlock)s.getBlock()).getTier()),
                    NsTutLibBlocks.OUTPUT_ENERGY_HATCH_TIER_1_BLOCK.get(),
                    NsTutLibBlocks.OUTPUT_ENERGY_HATCH_TIER_2_BLOCK.get(),
                    NsTutLibBlocks.OUTPUT_ENERGY_HATCH_TIER_3_BLOCK.get(),
                    NsTutLibBlocks.OUTPUT_ENERGY_HATCH_TIER_4_BLOCK.get()
            ).build(null));

    // Platform-specific factory methods, using @ExpectPlatform
    @ExpectPlatform
    public static ItemHatchBlockEntity createItemHatchBE(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FluidHatchBlockEntity createFluidHatchBE(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static EnergyHatchBlockEntity getPlatformSpecificInputEnergyHatchBE(BlockPos pos, BlockState state, EnergyTier tier) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static EnergyHatchBlockEntity getPlatformSpecificOutputEnergyHatchBE(BlockPos pos, BlockState state, EnergyTier tier) {
        throw new AssertionError();
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
