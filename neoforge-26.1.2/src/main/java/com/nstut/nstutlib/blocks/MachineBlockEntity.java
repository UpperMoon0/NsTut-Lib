package com.nstut.nstutlib.blocks;

import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.recipes.ModRecipe;
import com.nstut.nstutlib.recipes.RecipeTransactionCorruptedException;
import com.nstut.nstutlib.recipes.RecipeTransactionException;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider, Multiblock {
    protected static final Logger LOGGER = Logger.getLogger(MachineBlockEntity.class.getName());
    private static final int ACTIVE_STRUCTURE_CHECK_INTERVAL = 0;
    private static final int IDLE_STRUCTURE_CHECK_INTERVAL = 20;
    private static final int PROCESSING_FAILURE_RETRY_INTERVAL = 20;

    protected MultiblockPattern multiblockPattern;

    @Getter private final int southOffsetX;
    @Getter private final int southOffsetY;
    @Getter private final int southOffsetZ;

    protected int energyConsumed;
    protected int recipeEnergyCost;
    protected boolean isStructureValid;
    protected Optional<? extends ModRecipe<?>> recipeHandler = Optional.empty();
    protected boolean ingredientsConsumed;

    private ResourceKey<Recipe<?>> activeRecipeKey;
    private int[] activeItemOutputIndexes;
    private int structureCheckCooldown;
    private int processingFailureCooldown;

    public MachineBlockEntity(BlockEntityType<? extends MachineBlockEntity> type, BlockPos pos, BlockState state,
                              int southOffsetX, int southOffsetY, int southOffsetZ) {
        super(type, pos, state);
        this.southOffsetX = southOffsetX;
        this.southOffsetY = southOffsetY;
        this.southOffsetZ = southOffsetZ;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyConsumed = Math.max(0, input.getIntOr("energyConsumed", 0));
        recipeEnergyCost = Math.max(0, input.getIntOr("recipeEnergyCost", 0));
        ingredientsConsumed = input.getBooleanOr("ingredientsConsumed", false);
        activeRecipeKey = null;
        Identifier id = Identifier.tryParse(input.getStringOr("activeRecipeId", ""));
        if (id != null) activeRecipeKey = ResourceKey.create(Registries.RECIPE, id);
        activeItemOutputIndexes = input.getIntArray("activeItemOutputIndexes").orElse(null);
        recipeHandler = Optional.empty();
        structureCheckCooldown = 0;
        processingFailureCooldown = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energyConsumed", Math.max(0, energyConsumed));
        output.putInt("recipeEnergyCost", Math.max(0, recipeEnergyCost));
        output.putBoolean("ingredientsConsumed", ingredientsConsumed);
        if (activeRecipeKey != null) output.putString("activeRecipeId", activeRecipeKey.identifier().toString());
        if (activeItemOutputIndexes != null) output.putIntArray("activeItemOutputIndexes", activeItemOutputIndexes);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        boolean previousStructureValid = blockEntity.isStructureValid;
        if (blockEntity.structureCheckCooldown <= 0) {
            blockEntity.isStructureValid = blockEntity.checkMultiblock(level, pos, state);
            blockEntity.structureCheckCooldown = blockEntity.hasActiveRecipe() ? ACTIVE_STRUCTURE_CHECK_INTERVAL : IDLE_STRUCTURE_CHECK_INTERVAL;
        } else {
            blockEntity.structureCheckCooldown--;
        }

        if (blockEntity.isStructureValid) {
            if (blockEntity.processingFailureCooldown > 0) {
                blockEntity.processingFailureCooldown--;
            } else {
                try {
                    blockEntity.setHatches(pos, level);
                    blockEntity.processRecipe(level, pos);
                } catch (RecipeTransactionCorruptedException exception) {
                    blockEntity.clearActiveRecipe();
                    blockEntity.processingFailureCooldown = PROCESSING_FAILURE_RETRY_INTERVAL;
                    LOGGER.log(java.util.logging.Level.SEVERE,
                            "Machine transaction rollback failed at " + pos + "; active recipe was cancelled to prevent duplicate output or repeated consumption",
                            exception);
                } catch (RecipeTransactionException exception) {
                    blockEntity.processingFailureCooldown = PROCESSING_FAILURE_RETRY_INTERVAL;
                    LOGGER.log(java.util.logging.Level.WARNING,
                            "Machine transaction failed safely at " + pos + "; preserving active recipe and retrying later", exception);
                } catch (ClassCastException | NullPointerException | IllegalStateException exception) {
                    blockEntity.isStructureValid = false;
                    blockEntity.structureCheckCooldown = 0;
                    LOGGER.log(java.util.logging.Level.WARNING,
                            "Machine hatch/capability became unavailable while processing at " + pos, exception);
                }
            }
        }

        boolean operating = blockEntity.isStructureValid && blockEntity.hasActiveRecipe();
        if (state.hasProperty(MachineBlock.OPERATING) && state.getValue(MachineBlock.OPERATING) != operating) {
            level.setBlock(pos, state.setValue(MachineBlock.OPERATING, operating), 3);
        }
        if (previousStructureValid != blockEntity.isStructureValid) blockEntity.setChanged();
    }

    private boolean hasActiveRecipe() {
        return recipeHandler.isPresent() || activeRecipeKey != null;
    }

    protected abstract void processRecipe(Level level, BlockPos blockPos);

    protected final <R extends ModRecipe<R>> void processRecipeTransaction(Level level, RecipeType<R> recipeType,
                                                                           IItemHandler inputSlots, List<? extends IFluidHandler> inputTanks,
                                                                           IItemHandler outputSlots, List<? extends IFluidHandler> outputTanks,
                                                                           IEnergyStorage energyStorage, int energyPerTick) {
        processRecipeTransaction(level, recipeType, inputSlots, inputTanks, outputSlots, outputTanks, energyStorage, energyPerTick, null);
    }

    protected final <R extends ModRecipe<R>> void processRecipeTransaction(Level level, RecipeType<R> recipeType,
                                                                           IItemHandler inputSlots, List<? extends IFluidHandler> inputTanks,
                                                                           IItemHandler outputSlots, List<? extends IFluidHandler> outputTanks,
                                                                           IEnergyStorage energyStorage, int energyPerTick,
                                                                           @Nullable Comparator<R> recipePreference) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ModRecipe.requireRestorableStorage(inputSlots, inputTanks, "input");
        ModRecipe.requireRestorableStorage(outputSlots, outputTanks, "output");
        restoreRecipeHandler(serverLevel, recipeType);

        if (recipeHandler.isEmpty()) {
            BlockState controllerState = level.getBlockState(worldPosition);
            if (!checkMultiblock(level, worldPosition, controllerState)) {
                isStructureValid = false;
                structureCheckCooldown = 0;
                return;
            }
            isStructureValid = true;

            Stream<RecipeHolder<R>> candidates = recipesFor(serverLevel, recipeType)
                    .filter(holder -> holder.value().recipeMatch(inputSlots, inputTanks, outputSlots, outputTanks));
            Optional<RecipeHolder<R>> nextRecipe = recipePreference == null
                    ? candidates.findFirst()
                    : candidates.max((left, right) -> recipePreference.compare(left.value(), right.value()));
            if (nextRecipe.isEmpty()) return;
            RecipeHolder<R> holder = nextRecipe.get();
            startRecipe(holder.id(), holder.value());
            structureCheckCooldown = ACTIVE_STRUCTURE_CHECK_INTERVAL;
        }

        if (recipeHandler.get().getType() != recipeType) {
            clearActiveRecipe();
            return;
        }

        @SuppressWarnings("unchecked") R activeRecipe = (R) recipeHandler.get();
        recipeEnergyCost = Math.max(0, activeRecipe.getTotalEnergy());
        ensureOutputRolls(activeRecipe);

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

        if (energyConsumed >= recipeEnergyCost && activeRecipe.canFitOutputs(outputSlots, outputTanks, activeItemOutputIndexes)) {
            activeRecipe.assemble(outputSlots, outputTanks, activeItemOutputIndexes);
            clearActiveRecipe();
        }
    }

    @SuppressWarnings("unchecked")
    private static <R extends ModRecipe<R>> Stream<RecipeHolder<R>> recipesFor(ServerLevel level, RecipeType<R> recipeType) {
        return level.recipeAccess().getRecipes().stream()
                .filter(holder -> holder.value().getType() == recipeType)
                .map(holder -> (RecipeHolder<R>) (RecipeHolder<?>) holder);
    }

    private <R extends ModRecipe<R>> void restoreRecipeHandler(ServerLevel level, RecipeType<R> expectedType) {
        if (recipeHandler.isPresent() || activeRecipeKey == null) return;

        Optional<RecipeHolder<?>> restored = level.recipeAccess().byKey(activeRecipeKey);
        if (restored.isPresent() && restored.get().value() instanceof ModRecipe<?> modRecipe && modRecipe.getType() == expectedType) {
            recipeHandler = Optional.of(modRecipe);
            recipeEnergyCost = Math.max(0, modRecipe.getTotalEnergy());
            energyConsumed = Math.min(energyConsumed, recipeEnergyCost);
            ensureOutputRolls(modRecipe);
        } else {
            LOGGER.warning("Unable to restore active recipe " + activeRecipeKey.identifier() + " at " + worldPosition);
            clearActiveRecipe();
        }
    }

    private void ensureOutputRolls(ModRecipe<?> recipe) {
        if (activeItemOutputIndexes == null || !recipe.areRolledItemOutputIndexesValid(activeItemOutputIndexes)) {
            activeItemOutputIndexes = recipe.rollItemOutputIndexes();
            setChanged();
        }
    }

    private void startRecipe(ResourceKey<Recipe<?>> recipeKey, ModRecipe<?> recipe) {
        recipeHandler = Optional.of(recipe);
        activeRecipeKey = recipeKey;
        activeItemOutputIndexes = recipe.rollItemOutputIndexes();
        recipeEnergyCost = Math.max(0, recipe.getTotalEnergy());
        energyConsumed = 0;
        ingredientsConsumed = false;
        processingFailureCooldown = 0;
        setChanged();
    }

    protected final void clearActiveRecipe() {
        recipeHandler = Optional.empty();
        activeRecipeKey = null;
        activeItemOutputIndexes = null;
        recipeEnergyCost = 0;
        energyConsumed = 0;
        ingredientsConsumed = false;
        processingFailureCooldown = 0;
        structureCheckCooldown = 0;
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

    protected EnumProperty<Direction> getFacingProperty() {
        return MachineBlock.FACING;
    }

    public final boolean checkMultiblock(Level level, BlockPos blockPos, BlockState blockState) {
        if (multiblockPattern == null) multiblockPattern = getMultiblockPattern();
        return multiblockPattern.check(level, blockPos, blockState, southOffsetX, southOffsetY, southOffsetZ);
    }

    public final void requestStructureValidation() {
        structureCheckCooldown = 0;
    }
}
