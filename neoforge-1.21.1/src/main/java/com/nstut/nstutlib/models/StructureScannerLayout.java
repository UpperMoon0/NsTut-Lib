package com.nstut.nstutlib.models;

/**
 * Maps scanned world coordinates into the canonical south-facing MultiblockPattern layout.
 * Pattern layers are top-first, X columns run west-to-east, and Z rows run north-to-south.
 */
public final class StructureScannerLayout {
    private StructureScannerLayout() {
    }

    public static int patternY(int worldY, int maxY) {
        return maxY - worldY;
    }

    public static int patternX(int worldX, int minX) {
        return worldX - minX;
    }

    public static int patternZ(int worldZ, int minZ) {
        return worldZ - minZ;
    }
}
