package com.nstut.nstutlib.models;

import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureScannerStateFilterTest {
    @Test
    void omitsRuntimeManagedStatesButKeepsAuthoredPlacementStates() {
        Map<String, String> farmland = StructureScannerStateFilter.exportStates(Blocks.FARMLAND.defaultBlockState());
        assertFalse(farmland.containsKey("moisture"));

        Map<String, String> leaves = StructureScannerStateFilter.exportStates(Blocks.OAK_LEAVES.defaultBlockState());
        assertFalse(leaves.containsKey("distance"));
        assertTrue(leaves.containsKey("persistent"));
        assertTrue(leaves.containsKey("waterlogged"));

        Map<String, String> stairs = StructureScannerStateFilter.exportStates(Blocks.OAK_STAIRS.defaultBlockState());
        assertFalse(stairs.containsKey("shape"));
        assertTrue(stairs.containsKey("facing"));
        assertTrue(stairs.containsKey("half"));
        assertTrue(stairs.containsKey("waterlogged"));

        Map<String, String> wall = StructureScannerStateFilter.exportStates(Blocks.COBBLESTONE_WALL.defaultBlockState());
        assertFalse(wall.containsKey("up"));
        assertFalse(wall.containsKey("north"));
        assertFalse(wall.containsKey("east"));
        assertFalse(wall.containsKey("south"));
        assertFalse(wall.containsKey("west"));
        assertTrue(wall.containsKey("waterlogged"));

        Map<String, String> fence = StructureScannerStateFilter.exportStates(Blocks.OAK_FENCE.defaultBlockState());
        assertFalse(fence.containsKey("north"));
        assertFalse(fence.containsKey("east"));
        assertFalse(fence.containsKey("south"));
        assertFalse(fence.containsKey("west"));
        assertTrue(fence.containsKey("waterlogged"));

        Map<String, String> bars = StructureScannerStateFilter.exportStates(Blocks.IRON_BARS.defaultBlockState());
        assertFalse(bars.containsKey("north"));
        assertFalse(bars.containsKey("east"));
        assertFalse(bars.containsKey("south"));
        assertFalse(bars.containsKey("west"));
        assertTrue(bars.containsKey("waterlogged"));
    }
}
