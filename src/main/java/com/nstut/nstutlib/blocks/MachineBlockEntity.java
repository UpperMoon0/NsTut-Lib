package com.nstut.nstutlib.blocks;

import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider, Multiblock {

    protected static final Logger LOGGER = Logger.getLogger(MachineBlockEntity.class.getName());
    private static final int ACTIVE_STRUCTURE_CHECK_INTERVAL = 4;
    private static final int IDLE_STRUCTURE_CHECK_INTERVAL = 20;

    protected MultiblockPattern multiblockPattern;

    @Getter
    private final int southOffsetX;
    @Getter
    private final int southOffsetY;
    @Getter
    private final int southOffsetZ;

    protected int energyConsumed;
    protected int recipeEnergyCost;
    protected boolean isStructureValid;
    protected Optional<? extends ModRecipe<?>> recipeHandler = Optional.empty();
    protected boolean ingredientsConsumed;

    private ResourceLocation activeRecipeId;
    private int structureCheckCooldown;

    public MachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> type,
                              BlockPos pos,
                              BlockState state,
                              int southOffsetX,
                              int southOffsetY,
                              int southOffsetZ) {
        super(type, pos, state);
        this.southOffsetX = southOffsetX;
        this.southOffsetY = southOffsetY;
        this.southOffsetZ = southOffsetZ;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyConsumed = Math.max(0, tag.getInt("energyConsumed"));
        recipeEnergyCost = Math.max(0, tag.getInt("recipeEnergyCost"));
        ingredientsConsumed = tag.getBoolean("ingredientsConsumed");
        activeRecipeId = tag.contains("activeRecipeId")
                ? ResourceLocation.tryParse(tag.getString("activeRecipeId"))
                : null;
        recipeHandler = Optional.empty();
        structureCheckCooldown = 0;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energyConsumed", Math.max(0, energyConsumed));
        tag.putInt("recipeEnergyCost", Math.max(0, recipeEnergyCost));
        tag.putBoolean("ingredientsConsumed", ingredientsConsumed);
        if (activeRecipeId != null) {
            tag.putString("activeRecipeId", activeRecipeId.toString());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        boolean previousStructureValid = blockEntity.isStructureValid;
        if (blockEntity.structureCheckCooldown <= 0) {
            blockEntity.isStructureValid = blockEntity.checkMultiblock(level, pos, state);
            blockEntity.structureCheckCooldown = blockEntity.recipeHandler.isPresent()
                    ? ACTIVE_STRUCTURE_CHECK_INTERVAL
                    : IDLE_STRUCTURE_CHECK_INTERVAL;
        } else {
            blockEntity.structureCheckCooldown--;
        }

        if (blockEntity.isStructureValid) {
            try {
                blockEntity.setHatches(pos, level);
                blockEntity.processRecipe(level, pos);
            } catch (ClassCastException | NullPointerException exception) {
                blockEntity.isStructureValid = false;
                blockEntity.structureCheckCooldown = 0;
                LOGGER.log(java.util.logging.Level.WARNING,
                        "Machine structure changed while processing at " + pos,
                        exception);
            }
        }

        boolean operating = blockEntity.isStructureValid && blockEntity.recipeHandler.isPresent();
        if (state.hasProperty(MachineBlock.OPERATING) && state.getValue(MachineBlock.OPERATING) != operating) {
            level.setBlock(pos, state.setValue(MachineBlock.OPERATING, operating), 3);
        }

        if (previousStructureValid != blockEntity.isStructureValid) {
            blockEntity.setChanged();
        }
    }

    protected abstract void processRecipe(Level level, BlockPos blockPos);

    protected final <R extends ModRecipe<R>> void processRecipeTransaction(Level level,
                                                                           RecipeType<R> recipeType,
                                                                           IItemHandler inputSlots,
                                                                           List<IFluidHandler> inputTanks,
                                                                           IItemHandler outputSlots,
                                                                           List<IFluidHandler> outputTanks,
                                                                           IEnergyStorage energyStorage,
                                                                           int energyPerTick) {
        restoreRecipeHandler(level, recipeType);

        if (recipeHandler.isEmpty()) {
            Optional<R> nextRecipe = level.getRecipeManager()
                    .getAllRecipesFor(recipeType)
                    .stream()
                    .filter(recipe -> recipe.recipeMatch(inputSlots, inputTanks, outputSlots, outputTanks))
                    .findFirst();
            if (nextRecipe.isEmpty()) {
                return;
            }
            startRecipe(nextRecipe.get());
        }

        if (recipeHandler.get().getType() != recipeType) {
            clearActiveRecipe();
            return;
        }

        @SuppressWarnings("unchecked")
        R activeRecipe = (R) recipeHandler.get();
        recipeEnergyCost = Math.max(0, activeRecipe.getTotalEnergy());

        if (!ingredientsConsumed) {
            if (!activeRecipe.recipeMatch(inputSlots, inputTanks, outputSlots, outputTanks)
                    || !activeRecipe.tryConsumeIngredients(inputSlots, inputTanks)) {
                clearActiveRecipe();
                return;
            }
            ingredientsConsumed = true;
            setChanged();
        }

        int remainingEnergy = Math.max(0, recipeEnergyCost - energyConsumed);
        if (remainingEnergy > 0 && energyStorage != null && energyPerTick > 0) {
            int requested = Math.min(Math.min(remainingEnergy, energyPerTick), energyStorage.getEnergyStored());
            if (requested > 0) {
                int extracted = energyStorage.extractEnergy(requested, false);
                if (extracted > 0) {
                    energyConsumed = Math.min(recipeEnergyCost, energyConsumed + extracted);
                    setChanged();
                }
            }
        }

        if (energyConsumed >= recipeEnergyCost && activeRecipe.canFitOutputs(outputSlots, outputTanks)) {
            activeRecipe.assemble(outputSlots, outputTanks);
            clearActiveRecipe();
        }
    }

    private <R extends ModRecipe<R>> void restoreRecipeHandler(Level level, RecipeType<R> expectedType) {
        if (recipeHandler.isPresent() || activeRecipeId == null) {
            return;
        }

        Optional<? extends Recipe<?>> restored = level.getRecipeManager().byKey(activeRecipeId);
        if (restored.isPresent()
                && restored.get() instanceof ModRecipe<?> modRecipe
                && modRecipe.getType() == expectedType) {
            recipeHandler = Optional.of(modRecipe);
            recipeEnergyCost = Math.max(0, modRecipe.getTotalEnergy());
            energyConsumed = Math.min(energyConsumed, recipeEnergyCost);
        } else {
            LOGGER.warning("Unable to restore active recipe " + activeRecipeId + " at " + worldPosition);
            clearActiveRecipe();
        }
    }

    private void startRecipe(ModRecipe<?> recipe) {
        recipeHandler = Optional.of(recipe);
        activeRecipeId = recipe.getId();
        recipeEnergyCost = Math.max(0, recipe.getTotalEnergy());
        energyConsumed = 0;
        ingredientsConsumed = false;
        setChanged();
    }

    protected final void clearActiveRecipe() {
        recipeHandler = Optional.empty();
        activeRecipeId = null;
        recipeEnergyCost = 0;
        energyConsumed = 0;
        ingredientsConsumed = false;
        setChanged();
    }

    protected Vec3i rotateHatchesOffset(Vec3i southOffset, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vec3i(-southOffset.getX(), southOffset.getY(), -southOffset.getZ());
            case WEST -> new Vec3i(-southOffset.getZ(), southOffset.getY(), southOffset.getX());
            case EAST -> new Vec3i(southOffset.getZ(), southOffset.getY(), -southOffset.getX());
            default -> southOffset;
        };
    }

    protected abstract void setHatches(BlockPos blockPos, Level level);

    protected DirectionProperty getFacingProperty() {
        return MachineBlock.FACING;
    }

    public final boolean checkMultiblock(Level level, BlockPos blockPos, BlockState blockState) {
        if (multiblockPattern == null) {
            multiblockPattern = getMultiblockPattern();
        }
        return multiblockPattern.check(level, blockPos, blockState, southOffsetX, southOffsetY, southOffsetZ);
    }

    public final void requestStructureValidation() {
        structureCheckCooldown = 0;
    }
}
