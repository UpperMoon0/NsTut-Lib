package com.nstut.nstutlib.machines.config;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.List;

public class MachineConfig {
    private String name;
    @SerializedName("required_hatches")
    private List<RequiredHatch> requiredHatches;
    private List<List<List<String>>> structure;
    @SerializedName("south_offset_x")
    private int southOffsetX;
    @SerializedName("south_offset_y")
    private int southOffsetY;
    @SerializedName("south_offset_z")
    private int southOffsetZ;

    public String getName() {
        return name;
    }

    public List<RequiredHatch> getRequiredHatches() {
        return requiredHatches;
    }

    public List<List<List<String>>> getStructure() {
        return structure;
    }

    public int getSouthOffsetX() {
        return southOffsetX;
    }

    public int getSouthOffsetY() {
        return southOffsetY;
    }

    public int getSouthOffsetZ() {
        return southOffsetZ;
    }

    public static class RequiredHatch {
        @SerializedName("relative_pos")
        private List<Integer> relativePos;
        private String type;
        private Direction direction;

        public Vec3i getRelativePos() {
            if (relativePos != null && relativePos.size() == 3) {
                return new Vec3i(relativePos.get(0), relativePos.get(1), relativePos.get(2));
            }
            return Vec3i.ZERO; // Default or error case
        }

        public String getType() {
            return type;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}