package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.blocks.hatch.*;
import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries; 
import com.nstut.nstutlib.util.EnergyTier;
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
            () -> new BlockItem(INPUT_ITEM_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    // Output Item Hatch
    public static final RegistrySupplier<Block> OUTPUT_ITEM_HATCH_BLOCK = BLOCKS.register("output_item_hatch",
            () -> new OutputItemHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> OUTPUT_ITEM_HATCH_ITEM = ITEMS.register("output_item_hatch",
            () -> new BlockItem(OUTPUT_ITEM_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    // Input Fluid Hatch
    public static final RegistrySupplier<Block> INPUT_FLUID_HATCH_BLOCK = BLOCKS.register("input_fluid_hatch",
            () -> new InputFluidHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> INPUT_FLUID_HATCH_ITEM = ITEMS.register("input_fluid_hatch",
            () -> new BlockItem(INPUT_FLUID_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    // Output Fluid Hatch
    public static final RegistrySupplier<Block> OUTPUT_FLUID_HATCH_BLOCK = BLOCKS.register("output_fluid_hatch",
            () -> new OutputFluidHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f)));
    public static final RegistrySupplier<Item> OUTPUT_FLUID_HATCH_ITEM = ITEMS.register("output_fluid_hatch",
            () -> new BlockItem(OUTPUT_FLUID_HATCH_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    // Energy Hatches
    public static final RegistrySupplier<Block> INPUT_ENERGY_HATCH_TIER_1_BLOCK = BLOCKS.register("input_energy_hatch_tier_1",
            () -> new InputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_1));
    public static final RegistrySupplier<Item> INPUT_ENERGY_HATCH_TIER_1_ITEM = ITEMS.register("input_energy_hatch_tier_1",
            () -> new BlockItem(INPUT_ENERGY_HATCH_TIER_1_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> INPUT_ENERGY_HATCH_TIER_2_BLOCK = BLOCKS.register("input_energy_hatch_tier_2",
            () -> new InputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_2));
    public static final RegistrySupplier<Item> INPUT_ENERGY_HATCH_TIER_2_ITEM = ITEMS.register("input_energy_hatch_tier_2",
            () -> new BlockItem(INPUT_ENERGY_HATCH_TIER_2_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> INPUT_ENERGY_HATCH_TIER_3_BLOCK = BLOCKS.register("input_energy_hatch_tier_3",
            () -> new InputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_3));
    public static final RegistrySupplier<Item> INPUT_ENERGY_HATCH_TIER_3_ITEM = ITEMS.register("input_energy_hatch_tier_3",
            () -> new BlockItem(INPUT_ENERGY_HATCH_TIER_3_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> INPUT_ENERGY_HATCH_TIER_4_BLOCK = BLOCKS.register("input_energy_hatch_tier_4",
            () -> new InputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_4));
    public static final RegistrySupplier<Item> INPUT_ENERGY_HATCH_TIER_4_ITEM = ITEMS.register("input_energy_hatch_tier_4",
            () -> new BlockItem(INPUT_ENERGY_HATCH_TIER_4_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> OUTPUT_ENERGY_HATCH_TIER_1_BLOCK = BLOCKS.register("output_energy_hatch_tier_1",
            () -> new OutputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_1));
    public static final RegistrySupplier<Item> OUTPUT_ENERGY_HATCH_TIER_1_ITEM = ITEMS.register("output_energy_hatch_tier_1",
            () -> new BlockItem(OUTPUT_ENERGY_HATCH_TIER_1_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> OUTPUT_ENERGY_HATCH_TIER_2_BLOCK = BLOCKS.register("output_energy_hatch_tier_2",
            () -> new OutputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_2));
    public static final RegistrySupplier<Item> OUTPUT_ENERGY_HATCH_TIER_2_ITEM = ITEMS.register("output_energy_hatch_tier_2",
            () -> new BlockItem(OUTPUT_ENERGY_HATCH_TIER_2_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> OUTPUT_ENERGY_HATCH_TIER_3_BLOCK = BLOCKS.register("output_energy_hatch_tier_3",
            () -> new OutputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_3));
    public static final RegistrySupplier<Item> OUTPUT_ENERGY_HATCH_TIER_3_ITEM = ITEMS.register("output_energy_hatch_tier_3",
            () -> new BlockItem(OUTPUT_ENERGY_HATCH_TIER_3_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static final RegistrySupplier<Block> OUTPUT_ENERGY_HATCH_TIER_4_BLOCK = BLOCKS.register("output_energy_hatch_tier_4",
            () -> new OutputEnergyHatchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f), EnergyTier.TIER_4));
    public static final RegistrySupplier<Item> OUTPUT_ENERGY_HATCH_TIER_4_ITEM = ITEMS.register("output_energy_hatch_tier_4",
            () -> new BlockItem(OUTPUT_ENERGY_HATCH_TIER_4_BLOCK.get(), new Item.Properties().arch$tab(CreativeTabRegistries.NSTUT_LIB_TAB)));

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
