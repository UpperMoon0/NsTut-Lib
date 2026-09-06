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
    }
}
