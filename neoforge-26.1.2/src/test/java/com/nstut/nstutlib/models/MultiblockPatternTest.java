package com.nstut.nstutlib.models;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockPatternTest {
    @Test
    void rotatesHorizontalDirectionsFromSouthReference() {
        assertEquals(Direction.WEST,
                MultiblockPattern.rotateHorizontalDirection(Direction.WEST, Direction.SOUTH));
        assertEquals(Direction.NORTH,
                MultiblockPattern.rotateHorizontalDirection(Direction.NORTH, Direction.SOUTH));
        assertEquals(Direction.EAST,
                MultiblockPattern.rotateHorizontalDirection(Direction.EAST, Direction.SOUTH));
    }

    @Test
    void rotatesRectangularLayersWithoutAssumingSquareDimensions() {
        MultiblockBlock[][][] data = new MultiblockBlock[1][2][3];
        MultiblockPattern pattern = new MultiblockPattern(data);
        pattern.rotate(1);
        assertEquals(3, pattern.getPattern()[0].length);
        assertEquals(2, pattern.getPattern()[0][0].length);
    }

    @Test
    void matchesOnlyAuthoredBlockStateProperties() {
        BlockState controller = withState(Blocks.FURNACE.defaultBlockState(), "facing", "south");

        MultiblockBlock farmland = new MultiblockBlock(Blocks.FARMLAND, Map.of());
        BlockState hydratedFarmland = withState(Blocks.FARMLAND.defaultBlockState(), "moisture", "7");
        assertTrue(MultiblockPattern.matchesBlock(farmland, hydratedFarmland, controller));

        MultiblockBlock stairs = new MultiblockBlock(Blocks.OAK_STAIRS,
                Map.of("facing", "south", "half", "bottom", "waterlogged", "false"));
        BlockState neighborShapedStairs = withState(
                withState(Blocks.OAK_STAIRS.defaultBlockState(), "facing", "south"),
                "shape", "outer_left");
        assertTrue(MultiblockPattern.matchesBlock(stairs, neighborShapedStairs, controller));
        assertFalse(MultiblockPattern.matchesBlock(stairs,
                withState(neighborShapedStairs, "half", "top"), controller));
        assertFalse(MultiblockPattern.matchesBlock(stairs, Blocks.COBBLESTONE.defaultBlockState(), controller));
    }

    private static BlockState withState(BlockState state, String propertyName, String value) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(propertyName);
        if (property == null) {
            throw new IllegalArgumentException("Missing property " + propertyName + " on " + state.getBlock());
        }
        return applyState(state, property, value);
    }

    private static <T extends Comparable<T>> BlockState applyState(BlockState state, Property<T> property, String value) {
        return property.getValue(value)
                .map(parsed -> state.setValue(property, parsed))
                .orElseThrow(() -> new IllegalArgumentException("Invalid value " + value + " for " + property.getName()));
    }
}
