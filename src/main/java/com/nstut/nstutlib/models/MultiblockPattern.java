package com.nstut.nstutlib.models;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class MultiblockPattern {

    private final MultiblockBlock[][][] pattern;
    private final boolean debug;

    public MultiblockPattern(MultiblockBlock[][][] pattern) {
        this.pattern = pattern;
        this.debug = false;
    }

    public MultiblockPattern(MultiblockBlock[][][] pattern, boolean debug) {
        this.pattern = pattern;
        this.debug = debug;
    }

    public MultiblockBlock[][][] getPattern() {
        return pattern;
    }

    public boolean check(Level level, BlockPos blockPos, BlockState blockState, int controllerHeight) {
        BlockPos currentBlockPos;
        MultiblockBlock[][][] patternCopy = copyPattern(pattern);

        // Rotate the pattern copy
        switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> {
                for (MultiblockBlock[][] layer : patternCopy) {
                    for (int k = 0; k < 2; k++) {
                        rotateBlockMatrix(layer);
                    }
                }
                if (debug) {
                    System.out.println("North");
                }
            }
            case EAST -> {
                for (MultiblockBlock[][] layer : patternCopy) {
                    rotateBlockMatrix(layer);
                }
                if (debug) {
                    System.out.println("East");
                }
            }
            case WEST -> {
                for (MultiblockBlock[][] layer : patternCopy) {
                    for (int k = 0; k < 3; k++) {
                        rotateBlockMatrix(layer);
                    }
                }
                if (debug) {
                    System.out.println("West");
                }
            }
            default -> {
                if (debug) {
                    System.out.println("South");
                }
            }
        }

        if (debug) {
            for (int y = 0; y < patternCopy.length; y++) {
                System.out.println("Layer " + (y + 1) + ":");
                for (int z = 0; z < patternCopy[0].length; z++) {
                    for (int x = 0; x < patternCopy[0][0].length; x++) {
                        MultiblockBlock block = patternCopy[patternCopy.length - 1 - y][z][x];
                        System.out.printf("%-40s | ", Objects.requireNonNullElse(block, "null"));
                    }
                    System.out.println();
                }
                System.out.println();
            }
        }

        // Loop the y dimension
        for (int y = 0; y < patternCopy.length; y++) {
            // Loop the z dimension
            for (int z = 0; z < patternCopy[0].length; z++) {
                // Loop the x dimension
                for (int x = 0; x < patternCopy[0][0].length; x++) {
                    // Check facing
                    switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
                        case NORTH -> {
                            currentBlockPos = new BlockPos(blockPos.getX() - patternCopy[0][0].length / 2 + x, blockPos.getY() - (controllerHeight - 1) + y, blockPos.getZ() + z);

                            if (patternCopy[patternCopy.length - 1 - y][z][x] != null && !level.getBlockState(currentBlockPos).is(patternCopy[patternCopy.length - 1 - y][z][x].getBlock())) {
                                if (debug)
                                    System.out.println("Invalid block at " + currentBlockPos + ". Expected " + patternCopy[patternCopy.length - 1 - y][z][x] + ", but found " + level.getBlockState(currentBlockPos));
                                return false;
                            }
                        }
                        case SOUTH -> {
                            currentBlockPos = new BlockPos(blockPos.getX() - patternCopy[0][0].length / 2 + x, blockPos.getY() - (controllerHeight - 1) + y, blockPos.getZ() - (patternCopy[0].length - 1) + z);

                            if (patternCopy[patternCopy.length - 1 - y][z][x] != null && !level.getBlockState(currentBlockPos).is(patternCopy[patternCopy.length - 1 - y][z][x].getBlock())) {
                                if (debug)
                                    System.out.println("Invalid block at " + currentBlockPos + ". Expected " + patternCopy[patternCopy.length - 1 - y][z][x] + ", but found " + level.getBlockState(currentBlockPos));
                                return false;
                            }
                        }
                        case EAST -> {
                            currentBlockPos = new BlockPos(blockPos.getX() - (patternCopy[0][0].length - 1) + x, blockPos.getY() - (controllerHeight - 1) + y, blockPos.getZ() - patternCopy[0].length / 2 + z);

                            if (patternCopy[patternCopy.length - 1 - y][z][x] != null && !level.getBlockState(currentBlockPos).is(patternCopy[patternCopy.length - 1 - y][z][x].getBlock())) {
                                if (debug)
                                    System.out.println("Invalid block at " + currentBlockPos + ". Expected " + patternCopy[patternCopy.length - 1 - y][z][x] + ", but found " + level.getBlockState(currentBlockPos));
                                return false;
                            }
                        }
                        case WEST -> {
                            currentBlockPos = new BlockPos(blockPos.getX() + x, blockPos.getY() - (controllerHeight - 1) + y, blockPos.getZ() - patternCopy[0].length / 2 + z);

                            if (patternCopy[patternCopy.length - 1 - y][z][x] != null && !level.getBlockState(currentBlockPos).is(patternCopy[patternCopy.length - 1 - y][z][x].getBlock())) {
                                if (debug)
                                    System.out.println("Invalid block at " + currentBlockPos + ". Expected " + patternCopy[patternCopy.length - 1 - y][z][x] + ", but found " + level.getBlockState(currentBlockPos));
                                return false;
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        return true;
    }

    private MultiblockBlock[][][] copyPattern(MultiblockBlock[][][] original) {
        MultiblockBlock[][][] copy = new MultiblockBlock[original.length][][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new MultiblockBlock[original[i].length][];
            for (int j = 0; j < original[i].length; j++) {
                copy[i][j] = original[i][j].clone();
            }
        }
        return copy;
    }

    private static void rotateBlockMatrix(MultiblockBlock[][] layer)
    {
        int size = layer.length;
        // Consider all squares one by one
        for (int x = 0; x < size / 2; x++) {
            // Consider elements in group of 4 in current square
            for (int y = x; y < size - x - 1; y++) {
                // Store current cell in temp variable
                MultiblockBlock temp = layer[x][y];

                // Move values from right to top
                layer[x][y] = layer[y][size - 1 - x];

                // Move values from bottom to right
                layer[y][size - 1 - x] = layer[size - 1 - x][size - 1 - y];

                // Move values from left to bottom
                layer[size - 1 - x][size - 1 - y] = layer[size - 1 - y][x];

                // Assign temp to left
                layer[size - 1 - y][x] = temp;
            }
        }
    }
}
