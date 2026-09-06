package com.nstut.nstutlib.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureScannerLayoutTest {
    @Test
    void mapsWorldCoordinatesToCanonicalSouthFacingPatternOrder() {
        int minX = 10;
        int maxX = 13;
        int minY = 20;
        int maxY = 22;
        int minZ = 30;
        int maxZ = 34;

        assertEquals(0, StructureScannerLayout.patternY(maxY, maxY));
        assertEquals(maxY - minY, StructureScannerLayout.patternY(minY, maxY));

        assertEquals(0, StructureScannerLayout.patternX(minX, minX));
        assertEquals(maxX - minX, StructureScannerLayout.patternX(maxX, minX));

        assertEquals(0, StructureScannerLayout.patternZ(minZ, minZ));
        assertEquals(maxZ - minZ, StructureScannerLayout.patternZ(maxZ, minZ));
    }
}
