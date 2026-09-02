package com.nstut.nstutlib.models;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiblockPatternTest {
    @Test
    void rotatesHorizontalDirectionsFromSouthReference() {
        assertEquals(Direction.EAST,
                MultiblockPattern.rotateHorizontalDirection(Direction.WEST, Direction.SOUTH));
        assertEquals(Direction.NORTH,
                MultiblockPattern.rotateHorizontalDirection(Direction.NORTH, Direction.SOUTH));
        assertEquals(Direction.WEST,
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
}
