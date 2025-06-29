package com.nstut.nstutlib.blocks;

import com.nstut.nstutlib.machines.config.MachineConfig;
import com.nstut.nstutlib.machines.config.MachineConfigLoader;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import com.nstut.nstutlib.blocks.IMachineHatch;
import com.nstut.nstutlib.machines.config.MachineConfig;
import com.nstut.nstutlib.machines.config.MachineConfigLoader;
import com.nstut.nstutlib.models.MultiblockPattern;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Logger;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider, Multiblock {

    protected static final Logger LOGGER = Logger.getLogger(MachineBlockEntity.class.getName());

    // Stores the structure pattern for the multiblock machine
    protected MultiblockPattern multiblockPattern;

    // Stores the loaded machine configuration
    protected MachineConfig machineConfig;

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
                               BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        loadMachineConfig(pBlockState);
        if (this.machineConfig != null) {
            this.multiblockPattern = new MultiblockPattern(this.machineConfig.getStructure());
        }
    }

    // Loads the machine configuration from JSON
    protected void loadMachineConfig(BlockState blockState) {
        if (level == null || level.isClientSide()) {
            return;
        }

        // Assuming machine type can be derived from the block state or block entity type
        // For example, if your block's registry name is "biotech:dark_chamber_machine_block"
        // you might extract "dark_chamber"
        String machineName = getMachineNameFromBlockState(blockState); // Implement this method
        ResourceLocation configLocation = new ResourceLocation("biotech", "machines/" + machineName + ".json");

        Optional<MachineConfig> configOptional = MachineConfigLoader.loadConfig(level.getServer().getResourceManager(), configLocation);
        if (configOptional.isPresent()) {
            this.machineConfig = configOptional.get();
            LOGGER.info("Successfully loaded machine config for " + machineName);
        } else {
            LOGGER.severe("Failed to load machine config for " + machineName + ". Machine will be disabled.");
            this.isStructureValid = false; // Disable machine if config fails to load
            // Optionally, set a block state property to indicate disabled state
            // level.setBlock(worldPosition, blockState.setValue(MachineBlock.DISABLED, true), 3);
        }
    }

    // Placeholder method to get machine name from block state.
    // You'll need to implement this based on how your machine blocks are named/identified.
    protected String getMachineNameFromBlockState(BlockState blockState) {
        // Example: if your block's registry name is "biotech:dark_chamber_machine_block"
        // you might extract "dark_chamber"
        return blockState.getBlock().builtInRegistryHolder().key().location().getPath().replace("_block", "");
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
            // Validate the multiblock structure based on a pattern and loaded config
            blockEntity.isStructureValid = blockEntity.checkMultiblock(level, blockPos, blockState);

            // If the multiblock structure is valid, continue processing the recipe
            if (blockEntity.isStructureValid && blockEntity.machineConfig != null) {
                blockEntity.setHatches(blockPos, level); // This will be updated to use config
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

    // Sets the hatches in the machine's structure based on the loaded configuration
    protected void setHatches(BlockPos blockPos, Level level) {
        // This method will be implemented by subclasses if they need to store specific hatch references.
        // The validation of hatches is already done in checkMultiblock.
        // Subclasses can override this to, for example, populate a map of hatches.
    }

    // Returns the facing property of the block (e.g., north, south, east, west)
    protected DirectionProperty getFacingProperty() {
        return MachineBlock.FACING;
    }

    public boolean checkMultiblock(Level level, BlockPos blockPos, BlockState blockState) {
        if (machineConfig == null) {
            LOGGER.severe("Machine config is null for " + blockPos + ". Cannot validate multiblock.");
            return false;
        }
        // Update multiblockPattern to use the structure from machineConfig
        this.multiblockPattern = new MultiblockPattern(machineConfig.getStructure());
        boolean structureMatches = multiblockPattern.check(level, blockPos, blockState,
                machineConfig.getSouthOffsetX(),
                machineConfig.getSouthOffsetY(),
                machineConfig.getSouthOffsetZ());

        if (!structureMatches) {
            return false;
        }

        // Validate required hatches based on config
        Direction machineFacing = blockState.getValue(getFacingProperty());
        for (MachineConfig.RequiredHatch requiredHatch : machineConfig.getRequiredHatches()) {
            Vec3i relativePos = rotateHatchesOffset(requiredHatch.getRelativePos(), machineFacing);
            BlockPos hatchPos = blockPos.offset(relativePos);
            BlockEntity hatchBlockEntity = level.getBlockEntity(hatchPos);

            if (!(hatchBlockEntity instanceof IMachineHatch)) {
                LOGGER.warning("Missing required hatch at " + hatchPos + " for type " + requiredHatch.getType());
                return false;
            }

            IMachineHatch machineHatch = (IMachineHatch) hatchBlockEntity;
            if (!machineHatch.getHatchType().equalsIgnoreCase(requiredHatch.getType()) ||
                machineHatch.getHatchDirection() != requiredHatch.getDirection()) {
                LOGGER.warning("Hatch at " + hatchPos + " does not match required type/direction. Expected type: " + requiredHatch.getType() + ", direction: " + requiredHatch.getDirection() + ". Found type: " + machineHatch.getHatchType() + ", direction: " + machineHatch.getHatchDirection());
                return false;
            }
        }
        return true;
    }
}