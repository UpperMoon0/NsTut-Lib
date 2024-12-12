package com.nstut.nstutlib.blocks;

import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Logger;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider, Multiblock {

    protected static final Logger LOGGER = Logger.getLogger(MachineBlockEntity.class.getName());

    // Stores the structure pattern for the multiblock machine
    protected MultiblockPattern multiblockPattern;

    @Getter
    private final int southOffsetX;

    @Getter
    private final int southOffsetY;

    @Getter
    private final int southOffsetZ;

    // Tracks the amount of energy consumed by the machine
    protected int energyConsumed;

    // Stores the energy cost required to complete the current recipe
    protected int recipeEnergyCost;

    // Indicates if the structure (multiblock) is valid or not
    protected boolean isStructureValid;

    // Holds the current recipe being processed, if any
    protected Optional<? extends ModRecipe<?>> recipeHandler = Optional.empty();

    // Constructor for the machine block entity, sets its position and block state
    public MachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> pType,
                              BlockPos pPos,
                              BlockState pBlockState,
                              int southOffsetX,
                              int southOffsetY,
                              int southOffsetZ) {
        super(pType, pPos, pBlockState);
        this.multiblockPattern = getMultiblockPattern();
        this.southOffsetX = southOffsetX;
        this.southOffsetY = southOffsetY;
        this.southOffsetZ = southOffsetZ;
    }

    // Loads persistent data from the NBT tag when the world loads
    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        energyConsumed = pTag.getInt("energyConsumed");
    }

    // Saves additional persistent data like energy consumed to the NBT tag
    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("energyConsumed", energyConsumed);
    }

    // Server-side tick method that handles updates to the block entity every tick
    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T bEntity) {
        MachineBlockEntity blockEntity = (MachineBlockEntity) bEntity;

        // Only process on the server side (not client)
        if (!level.isClientSide) {
            // Validate the multiblock structure based on a pattern
            blockEntity.isStructureValid = blockEntity.checkMultiblock(level, blockPos, blockState);

            // If the multiblock structure is valid, continue processing the recipe
            if (blockEntity.isStructureValid) {
                blockEntity.setHatches(blockPos, level);
                blockEntity.processRecipe(level, blockPos);

                // Mark the block state as changed
                setChanged(level, blockPos, blockState);
            } else {
                // Reset energy and recipe cost if the structure is invalid
                blockEntity.energyConsumed = 0;
                blockEntity.recipeEnergyCost = 0;
            }

            // Update block state to indicate if the machine is operating (has a recipe)
            BlockState state;
            if (blockEntity.recipeHandler.isPresent()) {
                state = blockState.setValue(MachineBlock.OPERATING, Boolean.TRUE);
            } else {
                state = blockState.setValue(MachineBlock.OPERATING, Boolean.FALSE);
            }
            level.setBlock(blockPos, state, 3);
        }
    }

    // Abstract method that each machine block entity must implement to process its recipe
    protected abstract void processRecipe(Level level, BlockPos blockPos);

    // Rotates the position offsets of hatches based on the block's facing direction
    protected Vec3i rotateHatchesOffset(Vec3i southOffset, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vec3i(-southOffset.getX(), southOffset.getY(), -southOffset.getZ());
            case WEST -> new Vec3i(-southOffset.getZ(), southOffset.getY(), southOffset.getX());
            case EAST -> new Vec3i(southOffset.getZ(), southOffset.getY(), -southOffset.getX());
            default -> southOffset;
        };
    }

    // Abstract method to set the hatches in the machine's structure
    protected abstract void setHatches(BlockPos blockPos, Level level);

    // Returns the facing property of the block (e.g., north, south, east, west)
    protected DirectionProperty getFacingProperty() {
        return MachineBlock.FACING;
    }

    public boolean checkMultiblock(Level level, BlockPos blockPos, BlockState blockState) {
        return multiblockPattern.check(level, blockPos, blockState, southOffsetX, southOffsetY, southOffsetZ);
    }
}