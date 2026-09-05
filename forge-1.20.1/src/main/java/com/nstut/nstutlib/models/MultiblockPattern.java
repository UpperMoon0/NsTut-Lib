package com.nstut.nstutlib.models;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;

@Getter
public class MultiblockPattern {

    private MultiblockBlock[][][] pattern;

    public MultiblockPattern(MultiblockBlock[][][] pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern cannot be null");
        }
        this.pattern = pattern;
    }

    public boolean check(Level level,
                         BlockPos controllerPos,
                         BlockState controllerState,
                         int southOffsetX,
                         int southOffsetY,
                         int southOffsetZ) {
        for (int y = 0; y < pattern.length; y++) {
            int patternY = pattern.length - 1 - y;
            MultiblockBlock[][] layer = pattern[patternY];
            for (int z = 0; z < layer.length; z++) {
                MultiblockBlock[] row = layer[z];
                for (int x = 0; x < row.length; x++) {
                    MultiblockBlock expected = row[x];
                    if (expected == null) {
                        continue;
                    }

                    BlockPos currentPos = rotateBlockPos(
                            controllerPos,
                            southOffsetX,
                            southOffsetY,
                            southOffsetZ,
                            pattern.length,
                            layer.length,
                            x,
                            patternY,
                            z,
                            controllerState);
                    BlockState actual = level.getBlockState(currentPos);
                    if (!matches(expected, actual, controllerState)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean matches(MultiblockBlock expected, BlockState actual, BlockState controllerState) {
        if (!actual.is(expected.getBlock())) {
            return false;
        }

        for (Map.Entry<String, String> stateEntry : expected.getStates().entrySet()) {
            // Controller operating state is runtime state, not a construction requirement.
            if ("operating".equals(stateEntry.getKey())) {
                continue;
            }

            Property<?> property = actual.getBlock().getStateDefinition().getProperty(stateEntry.getKey());
            if (property == null) {
                return false;
            }

            String expectedValue = stateEntry.getValue();
            if ("facing".equals(stateEntry.getKey())
                    && controllerState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                Direction authoredDirection = Direction.byName(expectedValue);
                if (authoredDirection != null && authoredDirection.getAxis().isHorizontal()) {
                    expectedValue = rotateHorizontalDirection(
                            controllerState.getValue(HorizontalDirectionalBlock.FACING),
                            authoredDirection).getName();
                }
            }

            if (!propertyMatches(actual, property, expectedValue)) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> boolean propertyMatches(BlockState state, Property<T> property, String expectedValue) {
        return property.getValue(expectedValue)
                .map(value -> state.getValue(property).equals(value))
                .orElse(false);
    }

    public void rotate(int direction) {
        int turns = Math.floorMod(direction, 4);
        for (int turn = 0; turn < turns; turn++) {
            for (int y = 0; y < pattern.length; y++) {
                pattern[y] = rotateLayerClockwise(pattern[y]);
            }
        }
    }

    private MultiblockBlock[][] rotateLayerClockwise(MultiblockBlock[][] layer) {
        if (layer.length == 0) {
            return layer;
        }
        int width = layer[0].length;
        for (MultiblockBlock[] row : layer) {
            if (row.length != width) {
                throw new IllegalArgumentException("Multiblock layers must be rectangular");
            }
        }

        MultiblockBlock[][] rotated = new MultiblockBlock[width][layer.length];
        for (int z = 0; z < layer.length; z++) {
            for (int x = 0; x < width; x++) {
                rotated[x][layer.length - 1 - z] = layer[z][x];
            }
        }
        return rotated;
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
        if (!controllerDirection.getAxis().isHorizontal() || !currentDirection.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Directions must be horizontal");
        }

        int controllerOffset = switch (controllerDirection) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> throw new IllegalArgumentException("Invalid controller direction");
        };

        Direction result = currentDirection;
        for (int i = 0; i < controllerOffset; i++) {
            result = result.getClockWise();
        }
        return result;
    }
}
