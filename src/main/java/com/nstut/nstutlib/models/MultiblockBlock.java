package com.nstut.nstutlib.models;

import lombok.Data;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Data
public class MultiblockBlock {

    private Block block;
    Map<String, String> states = new HashMap<>();

    public MultiblockBlock(Block block) {
        this.block = block;
    }

    public MultiblockBlock(Block block, Map<String, String> states) {
        this.block = block;
        this.states = states;
    }

    public MultiblockBlock(String blockId) {
        this.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (this.block == null) {
            // Handle case where block is not found, e.g., log a warning or throw an exception
            System.err.println("Warning: Block with ID " + blockId + " not found in registry.");
        }
    }
}
