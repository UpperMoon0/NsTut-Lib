package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

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


    private static ItemHatchBlockEntity createItemHatchBE(BlockPos pos, BlockState state) {
        return getPlatformSpecificItemHatchBE(pos, state);
    }

    private static FluidHatchBlockEntity createFluidHatchBE(BlockPos pos, BlockState state) {
        return getPlatformSpecificFluidHatchBE(pos, state);
    }

    @ExpectPlatform
    public static ItemHatchBlockEntity getPlatformSpecificItemHatchBE(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FluidHatchBlockEntity getPlatformSpecificFluidHatchBE(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
