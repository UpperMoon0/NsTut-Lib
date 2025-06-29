package com.nstut.nstutlib.models;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.logging.Logger;

@Getter
public class MultiblockPattern {

    private static final Logger LOGGER = Logger.getLogger(MultiblockPattern.class.getName());

    private final MultiblockBlock[][][] pattern;

    public MultiblockPattern(MultiblockBlock[][][] pattern) {
        this.pattern = pattern;
    }

    public MultiblockPattern(java.util.List<java.util.List<java.util.List<String>>> stringPattern) {
        int height = stringPattern.size();
        int depth = stringPattern.get(0).size();
        int width = stringPattern.get(0).get(0).size();
        this.pattern = new MultiblockBlock[height][depth][width];

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    String blockId = stringPattern.get(y).get(z).get(x);
                    if (!blockId.equals("air")) { // Assuming "air" means no block
                        // You'll need a way to get a Block object from a string ID.
                        // This might involve a registry lookup or a custom mapping.
                        // For now, let's assume MultiblockBlock can be created with a string ID.
                        this.pattern[y][z][x] = new MultiblockBlock(blockId);
                    } else {
                        this.pattern[y][z][x] = null; // Represent air as null
                    }
                }
            }
        }
    }

    public boolean check(Level level,
                         BlockPos controllerPos,
                         BlockState blockState,
                         int southOffsetX,
                         int southOffsetY,
                         int southOffsetZ) {
        MultiblockPattern multiPatternCopy = new MultiblockPattern(copyPattern(this.pattern));
        MultiblockBlock[][][] patternCopy = multiPatternCopy.pattern;

        for (int y = 0; y < patternCopy.length; y++) {
            for (int z = 0; z < patternCopy[0].length; z++) {
                for (int x = 0; x < patternCopy[0][0].length; x++) {
                    // Calculate the position relative to the controller
                    BlockPos currentBlockPos = rotateBlockPos(
                            controllerPos,
                            southOffsetX,
                            southOffsetY,
                            southOffsetZ,
                            patternCopy.length,
                            patternCopy[0].length,
                            x,
                            patternCopy.length - 1 - y,
                            z,
                            blockState
                    );

                    // Check if the current block matches the pattern
                    if (patternCopy[patternCopy.length - 1 - y][z][x] != null
                            && !level.getBlockState(currentBlockPos).is(patternCopy[patternCopy.length - 1 - y][z][x].getBlock())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Rotates the entire multiblock by rotating all its layers.
     *
     * @param direction the number of 90-degree clockwise rotations (0 to 3).
     */
    public void rotate(int direction) {
        for (MultiblockBlock[][] layer : this.pattern) {
            rotateLayer(layer, direction);
        }
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

    private void rotateLayer(MultiblockBlock[][] layer, int direction) {
        int size = layer.length;
        for (int i = 0; i < direction; i++) {
            for (int x = 0; x < size / 2; x++) {
                for (int y = x; y < size - x - 1; y++) {
                    MultiblockBlock temp = layer[x][y];
                    layer[x][y] = layer[y][size - 1 - x];
                    layer[y][size - 1 - x] = layer[size - 1 - x][size - 1 - y];
                    layer[size - 1 - x][size - 1 - y] = layer[size - 1 - y][x];
                    layer[size - 1 - y][x] = temp;
                }
            }
        }
    }

    public static BlockPos rotateBlockPos(BlockPos controllerPos,
                                          int southOffsetX,
                                          int southOffsetY,
                                          int southOffsetZ,
                                          int structureHeight,
                                          int structureDepth,
                                          int southIndexX,
                                          int southIndexY,
                                          int southIndexZ,
                                          BlockState controllerBlockState) {
        int dx = southIndexX - southOffsetX;
        int dy = structureHeight - southIndexY - southOffsetY - 1;
        int dz = structureDepth - southIndexZ - southOffsetZ - 1;

        return switch (controllerBlockState.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> controllerPos.offset(-dx, dy, dz);
            case SOUTH -> controllerPos.offset(dx, dy, -dz);
            case WEST -> controllerPos.offset(dz, dy, dx);
            case EAST -> controllerPos.offset(-dz, dy, -dx);
            default -> controllerPos.offset(dx, dy, -dz);
        };
    }

    public static Direction rotateHorizontalDirection(Direction controllerDirection, Direction currentDirection) {
        // Determine the number of clockwise steps the controller has rotated
        int controllerOffset = switch (controllerDirection) {
            case SOUTH -> 0; // No rotation needed when controller faces south
            case WEST -> 1; // 90 degrees clockwise
            case NORTH -> 2; // 180 degrees clockwise
            case EAST -> 3; // 270 degrees clockwise
            default -> throw new IllegalArgumentException("Invalid controller direction");
        };

        // Rotate the currentDirection based on the controller's rotation
        Direction result = currentDirection;
        for (int i = 0; i < controllerOffset; i++) {
            result = result.getClockWise();
        }

        return result;
    }
}
