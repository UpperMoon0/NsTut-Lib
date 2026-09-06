package com.nstut.nstutlib.models;

import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StructureScannerStateFilter {
    private static final Set<String> WALL_CONNECTION_PROPERTIES = Set.of("up", "north", "east", "south", "west");

    private StructureScannerStateFilter() {
    }

    public static Map<String, String> exportStates(BlockState state) {
        Map<String, String> states = new LinkedHashMap<>();
        for (Property<?> property : state.getProperties()) {
            if (shouldExport(state, property)) {
                states.put(property.getName(), propertyValue(state, property));
            }
        }
        return states;
    }

    private static boolean shouldExport(BlockState state, Property<?> property) {
        String name = property.getName();
        if (state.getBlock() instanceof FarmBlock && "moisture".equals(name)) {
            return false;
        }
        if (state.getBlock() instanceof StairBlock && "shape".equals(name)) {
            return false;
        }
        return !(state.getBlock() instanceof WallBlock) || !WALL_CONNECTION_PROPERTIES.contains(name);
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
