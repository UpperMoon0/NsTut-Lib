package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.blocks.hatch.*;
import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries; // Assuming ITEM_GROUP is here
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class NsTutLibBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(NsTutLib.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(NsTutLib.MOD_ID, Registries.ITEM);

    // Input Item Hatch
    public static final RegistrySupplier<Block> INPUT_ITEM_HATCH_BLOCK = BLOCKS.register("input_item_hatch",
            () -> new InputItemHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> INPUT_ITEM_HATCH_ITEM = ITEMS.register("input_item_hatch",
            () -> new BlockItem(INPUT_ITEM_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.ITEM_GROUP)));

    // Output Item Hatch
    public static final RegistrySupplier<Block> OUTPUT_ITEM_HATCH_BLOCK = BLOCKS.register("output_item_hatch",
            () -> new OutputItemHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> OUTPUT_ITEM_HATCH_ITEM = ITEMS.register("output_item_hatch",
            () -> new BlockItem(OUTPUT_ITEM_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.ITEM_GROUP)));

    // Input Fluid Hatch
    public static final RegistrySupplier<Block> INPUT_FLUID_HATCH_BLOCK = BLOCKS.register("input_fluid_hatch",
            () -> new InputFluidHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> INPUT_FLUID_HATCH_ITEM = ITEMS.register("input_fluid_hatch",
            () -> new BlockItem(INPUT_FLUID_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.ITEM_GROUP)));

    // Output Fluid Hatch
    public static final RegistrySupplier<Block> OUTPUT_FLUID_HATCH_BLOCK = BLOCKS.register("output_fluid_hatch",
            () -> new OutputFluidHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> OUTPUT_FLUID_HATCH_ITEM = ITEMS.register("output_fluid_hatch",
            () -> new BlockItem(OUTPUT_FLUID_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.ITEM_GROUP)));

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
