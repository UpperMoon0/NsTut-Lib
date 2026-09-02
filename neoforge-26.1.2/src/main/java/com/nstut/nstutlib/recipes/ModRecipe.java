package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ModRecipe<T extends ModRecipe<T>> implements Recipe<RecipeInput>, RecipeFactory<T> {
    protected final Identifier id;
    @Getter
    protected final ModRecipeData recipe;
    private final RecipeSerializer<T> serializer;
    private final RecipeType<T> type;

    protected ModRecipe(Identifier id, ModRecipeData recipe, RecipeSerializer<T> serializer, RecipeType<T> type) {
        this.id = id;
        this.recipe = recipe;
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public T create(Identifier id, ModRecipeData recipeData) {
        if (recipeData == null) throw new IllegalArgumentException("Recipe data cannot be null");
        return createInstance(id, recipeData);
    }

    protected abstract T createInstance(Identifier id, ModRecipeData recipeContainer);

    @Override
    public @NotNull RecipeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public @NotNull RecipeType<T> getType() {
        return type;
    }

    /** Compatibility/debug identifier; authoritative identity lives on RecipeHolder. */
    public @NotNull Identifier getId() {
        return id;
    }

    public List<IngredientItem> getItemIngredients() { return List.of(recipe.getIngredientItems()); }
    public List<FluidStack> getFluidIngredients() { return List.of(recipe.getFluidIngredients()); }
    public List<OutputItem> getItemOutputs() { return List.of(recipe.getOutputItems()); }
    public List<FluidStack> getFluidOutputs() { return List.of(recipe.getFluidOutputs()); }

    public boolean recipeMatch(IItemHandler inputSlots,
                               List<? extends IFluidHandler> inputTanks,
                               IItemHandler outputSlots,
                               List<? extends IFluidHandler> outputTanks) {
        return hasRequiredItems(inputSlots) && hasRequiredFluids(inputTanks) && canFitOutputs(outputSlots, outputTanks);
    }

    private boolean hasRequiredItems(IItemHandler inputSlots) {
        IngredientItem[] ingredients = recipe.getIngredientItems();
        if (ingredients.length == 0) return true;
        if (inputSlots == null) return false;
        for (ItemStack required : aggregateRequiredItemStacks()) {
            int remaining = required.getCount();
            for (int slot = 0; slot < inputSlots.getSlots() && remaining > 0; slot++) {
                ItemStack present = inputSlots.getStackInSlot(slot);
                if (sameItem(required, present)) remaining -= Math.min(remaining, present.getCount());
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private List<ItemStack> aggregateRequiredItemStacks() {
        List<ItemStack> requiredStacks = new ArrayList<>();
        for (IngredientItem ingredient : recipe.getIngredientItems()) {
            ItemStack ingredientStack = ingredient.getItemStack();
            ItemStack aggregate = requiredStacks.stream().filter(existing -> sameItem(existing, ingredientStack)).findFirst().orElse(null);
            if (aggregate == null) requiredStacks.add(ingredientStack.copy());
            else aggregate.setCount(aggregate.getCount() + ingredientStack.getCount());
        }
        return requiredStacks;
    }

    private boolean hasRequiredFluids(List<? extends IFluidHandler> inputTanks) {
        FluidStack[] ingredients = recipe.getFluidIngredients();
        if (ingredients.length == 0) return true;
        if (inputTanks == null || inputTanks.isEmpty()) return false;
        for (FluidStack required : aggregateRequiredFluidStacks()) {
            int remaining = required.getAmount();
            for (IFluidHandler handler : inputTanks) {
                for (int tank = 0; tank < handler.getTanks() && remaining > 0; tank++) {
                    FluidStack present = handler.getFluidInTank(tank);
                    if (sameFluid(required, present)) remaining -= Math.min(remaining, present.getAmount());
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private List<FluidStack> aggregateRequiredFluidStacks() {
        List<FluidStack> requiredStacks = new ArrayList<>();
        for (FluidStack ingredient : recipe.getFluidIngredients()) {
            FluidStack aggregate = requiredStacks.stream().filter(existing -> sameFluid(existing, ingredient)).findFirst().orElse(null);
            if (aggregate == null) requiredStacks.add(ingredient.copy());
            else aggregate.grow(ingredient.getAmount());
        }
        return requiredStacks;
    }

    public int[] rollItemOutputIndexes() {
        OutputItem[] outputs = recipe.getOutputItems();
        int[] selected = new int[outputs.length];
        int selectedCount = 0;
        for (int index = 0; index < outputs.length; index++) {
            float chance = outputs[index].getChance();
            if (chance >= 1.0f || (chance > 0.0f && ThreadLocalRandom.current().nextFloat() < chance)) {
                selected[selectedCount++] = index;
            }
        }
        int[] result = new int[selectedCount];
        System.arraycopy(selected, 0, result, 0, selectedCount);
        return result;
    }

    public boolean areRolledItemOutputIndexesValid(int[] selectedIndexes) {
        if (selectedIndexes == null) return false;
        int outputCount = recipe.getOutputItems().length;
        boolean[] seen = new boolean[outputCount];
        for (int index : selectedIndexes) {
            if (index < 0 || index >= outputCount || seen[index]) return false;
            seen[index] = true;
        }
        return true;
    }

    public boolean canFitOutputs(IItemHandler outputSlots, List<? extends IFluidHandler> outputTanks) {
        return itemOutputsFit(outputSlots, null) && fluidOutputsFit(outputTanks);
    }

    public boolean canFitOutputs(IItemHandler outputSlots,
                                 List<? extends IFluidHandler> outputTanks,
                                 int[] selectedItemOutputIndexes) {
        return areRolledItemOutputIndexesValid(selectedItemOutputIndexes)
                && itemOutputsFit(outputSlots, selectedItemOutputIndexes)
                && fluidOutputsFit(outputTanks);
    }

    private boolean itemOutputsFit(IItemHandler outputSlots, int[] selectedItemOutputIndexes) {
        OutputItem[] outputs = recipe.getOutputItems();
        if (outputs.length == 0) return true;
        if (outputSlots == null) return false;
        boolean[] selected = selectionMask(outputs.length, selectedItemOutputIndexes);
        ItemStack[] virtualSlots = new ItemStack[outputSlots.getSlots()];
        for (int slot = 0; slot < outputSlots.getSlots(); slot++) virtualSlots[slot] = outputSlots.getStackInSlot(slot).copy();
        for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
            if (!selected[outputIndex]) continue;
            ItemStack remaining = outputs[outputIndex].getItemStack().copy();
            for (int slot = 0; slot < virtualSlots.length && !remaining.isEmpty(); slot++) {
                if (!outputSlots.isItemValid(slot, remaining)) continue;
                ItemStack present = virtualSlots[slot];
                int slotLimit = Math.min(outputSlots.getSlotLimit(slot), remaining.getMaxStackSize());
                if (present.isEmpty()) {
                    int moved = Math.min(slotLimit, remaining.getCount());
                    ItemStack inserted = remaining.copy();
                    inserted.setCount(moved);
                    virtualSlots[slot] = inserted;
                    remaining.shrink(moved);
                } else if (sameItem(present, remaining)) {
                    int max = Math.min(outputSlots.getSlotLimit(slot), present.getMaxStackSize());
                    int moved = Math.min(Math.max(0, max - present.getCount()), remaining.getCount());
                    if (moved > 0) {
                        present.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            if (!remaining.isEmpty()) return false;
        }
        return true;
    }

    private boolean fluidOutputsFit(List<? extends IFluidHandler> outputTanks) {
        FluidStack[] outputs = recipe.getFluidOutputs();
        if (outputs.length == 0) return true;
        if (outputTanks == null || outputTanks.isEmpty()) return false;
        List<VirtualFluidTank> virtualTanks = new ArrayList<>();
        for (IFluidHandler handler : outputTanks) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                virtualTanks.add(new VirtualFluidTank(handler, tank, handler.getTankCapacity(tank), handler.getFluidInTank(tank).copy()));
            }
        }
        for (FluidStack output : outputs) {
            FluidStack remaining = output.copy();
            for (VirtualFluidTank tank : virtualTanks) {
                if (remaining.isEmpty()) break;
                if (tank.fluid.isEmpty()) {
                    if (!tank.handler.isFluidValid(tank.index, remaining)) continue;
                    int moved = Math.min(tank.capacity, remaining.getAmount());
                    tank.fluid = remaining.copyWithAmount(moved);
                    remaining.shrink(moved);
                } else if (sameFluid(tank.fluid, remaining)) {
                    int moved = Math.min(Math.max(0, tank.capacity - tank.fluid.getAmount()), remaining.getAmount());
                    if (moved > 0) {
                        tank.fluid.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            if (!remaining.isEmpty()) return false;
        }
        return true;
    }

    public static void requireRestorableStorage(IItemHandler itemHandler,
                                                List<? extends IFluidHandler> fluidHandlers,
                                                String role) {
        if (itemHandler != null && !(itemHandler instanceof IItemHandlerModifiable)) {
            throw new IllegalArgumentException("Transactional " + role + " item handler must implement IItemHandlerModifiable");
        }
        if (fluidHandlers != null) {
            for (IFluidHandler handler : fluidHandlers) {
                if (!(handler instanceof FluidTank)) {
                    throw new IllegalArgumentException("Transactional " + role + " fluid handler must be a FluidTank or subclass");
                }
            }
        }
    }

    public void assemble(IItemHandler outputSlots, List<? extends IFluidHandler> outputTanks) {
        assemble(outputSlots, outputTanks, rollItemOutputIndexes());
    }

    public void assemble(IItemHandler outputSlots,
                         List<? extends IFluidHandler> outputTanks,
                         int[] selectedItemOutputIndexes) {
        requireRestorableStorage(outputSlots, outputTanks, "output");
        if (!areRolledItemOutputIndexesValid(selectedItemOutputIndexes)) {
            throw new IllegalArgumentException("Invalid persisted item-output selection for recipe " + id);
        }
        if (!canFitOutputs(outputSlots, outputTanks, selectedItemOutputIndexes)) {
            throw new RecipeTransactionException("Recipe outputs no longer fit: " + id);
        }
        MutableStateSnapshot snapshot = MutableStateSnapshot.capture(outputSlots, outputTanks);
        try {
            assembleUnchecked(outputSlots, outputTanks, selectedItemOutputIndexes);
        } catch (RecipeTransactionCorruptedException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            snapshot.rollbackOrThrow(failure, id, "output");
            throw new RecipeTransactionException("Recipe output transaction rolled back: " + id, failure);
        }
    }

    private void assembleUnchecked(IItemHandler outputSlots,
                                   List<? extends IFluidHandler> outputTanks,
                                   int[] selectedItemOutputIndexes) {
        OutputItem[] outputs = recipe.getOutputItems();
        boolean[] selected = selectionMask(outputs.length, selectedItemOutputIndexes);
        if (outputSlots != null) {
            for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
                if (!selected[outputIndex]) continue;
                ItemStack remaining = outputs[outputIndex].getItemStack().copy();
                for (int slot = 0; slot < outputSlots.getSlots() && !remaining.isEmpty(); slot++) {
                    remaining = outputSlots.insertItem(slot, remaining, false);
                }
                if (!remaining.isEmpty()) throw new RecipeTransactionException("Output inventory changed during recipe commit: " + id);
            }
        }
        for (FluidStack output : recipe.getFluidOutputs()) {
            FluidStack remaining = output.copy();
            for (IFluidHandler handler : safeFluidHandlers(outputTanks)) {
                if (remaining.isEmpty()) break;
                int accepted = handler.fill(remaining, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) continue;
                FluidStack portion = remaining.copyWithAmount(Math.min(accepted, remaining.getAmount()));
                int filled = handler.fill(portion, IFluidHandler.FluidAction.EXECUTE);
                if (filled != portion.getAmount()) {
                    throw new RecipeTransactionException("Fluid output handler diverged during recipe commit: " + id);
                }
                remaining.shrink(filled);
            }
            if (!remaining.isEmpty()) throw new RecipeTransactionException("Output fluid handlers changed during recipe commit: " + id);
        }
    }

    public boolean tryConsumeIngredients(IItemHandler inputSlots, List<? extends IFluidHandler> inputTanks) {
        requireRestorableStorage(inputSlots, inputTanks, "input");
        if (!hasRequiredItems(inputSlots) || !hasRequiredFluids(inputTanks)) return false;
        MutableStateSnapshot snapshot = MutableStateSnapshot.capture(inputSlots, inputTanks);
        try {
            consumeIngredientsUnchecked(inputSlots, inputTanks);
            return true;
        } catch (RecipeTransactionCorruptedException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            snapshot.rollbackOrThrow(failure, id, "input");
            throw new RecipeTransactionException("Recipe input transaction rolled back: " + id, failure);
        }
    }

    private void consumeIngredientsUnchecked(IItemHandler inputSlots, List<? extends IFluidHandler> inputTanks) {
        for (IngredientItem ingredient : recipe.getIngredientItems()) {
            if (!ingredient.isConsumable()) continue;
            ItemStack required = ingredient.getItemStack();
            int remaining = required.getCount();
            for (int slot = 0; slot < inputSlots.getSlots() && remaining > 0; slot++) {
                ItemStack present = inputSlots.getStackInSlot(slot);
                if (!sameItem(required, present)) continue;
                ItemStack extracted = inputSlots.extractItem(slot, remaining, false);
                if (!extracted.isEmpty() && !sameItem(required, extracted)) {
                    throw new RecipeTransactionException("Item input handler returned the wrong stack during recipe commit: " + id);
                }
                remaining -= extracted.getCount();
            }
            if (remaining > 0) throw new RecipeTransactionException("Item input handler changed during recipe commit: " + id);
        }
        for (FluidStack ingredient : recipe.getFluidIngredients()) {
            int remaining = ingredient.getAmount();
            for (IFluidHandler handler : safeFluidHandlers(inputTanks)) {
                if (remaining <= 0) break;
                FluidStack request = ingredient.copyWithAmount(remaining);
                FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty() && !sameFluid(ingredient, drained)) {
                    throw new RecipeTransactionException("Fluid input handler returned the wrong fluid during recipe commit: " + id);
                }
                if (sameFluid(ingredient, drained)) remaining -= drained.getAmount();
            }
            if (remaining > 0) throw new RecipeTransactionException("Fluid input handler changed during recipe commit: " + id);
        }
    }

    public void consumeIngredients(IItemHandler inputSlots, List<? extends IFluidHandler> inputTanks) {
        if (!tryConsumeIngredients(inputSlots, inputTanks)) {
            throw new RecipeTransactionException("Recipe inputs are no longer available: " + id);
        }
    }

    private static boolean[] selectionMask(int outputCount, int[] selectedItemOutputIndexes) {
        boolean[] selected = new boolean[outputCount];
        if (selectedItemOutputIndexes == null) {
            for (int index = 0; index < outputCount; index++) selected[index] = true;
            return selected;
        }
        for (int index : selectedItemOutputIndexes) if (index >= 0 && index < outputCount) selected[index] = true;
        return selected;
    }

    private static List<? extends IFluidHandler> safeFluidHandlers(List<? extends IFluidHandler> handlers) {
        return handlers == null ? Collections.emptyList() : handlers;
    }

    private static boolean sameItem(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && ItemStack.isSameItemSameComponents(first, second);
    }

    private static boolean sameFluid(FluidStack first, FluidStack second) {
        return !first.isEmpty() && !second.isEmpty() && FluidStack.isSameFluidSameComponents(first, second);
    }

    public int getTotalEnergy() { return recipe.getTotalEnergy(); }

    @Override public boolean matches(@NotNull RecipeInput input, @NotNull Level level) { return false; }
    @Override public @NotNull ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }

    private static final class MutableStateSnapshot {
        private final IItemHandlerModifiable items;
        private final ItemStack[] itemStacks;
        private final List<FluidState> fluids;

        private MutableStateSnapshot(IItemHandlerModifiable items, ItemStack[] itemStacks, List<FluidState> fluids) {
            this.items = items;
            this.itemStacks = itemStacks;
            this.fluids = fluids;
        }

        private static MutableStateSnapshot capture(IItemHandler itemHandler, List<? extends IFluidHandler> fluidHandlers) {
            IItemHandlerModifiable mutableItems = itemHandler == null ? null : (IItemHandlerModifiable) itemHandler;
            ItemStack[] itemStacks = new ItemStack[0];
            if (mutableItems != null) {
                itemStacks = new ItemStack[mutableItems.getSlots()];
                for (int slot = 0; slot < mutableItems.getSlots(); slot++) itemStacks[slot] = mutableItems.getStackInSlot(slot).copy();
            }
            List<FluidState> fluids = new ArrayList<>();
            for (IFluidHandler handler : safeFluidHandlers(fluidHandlers)) {
                FluidTank tank = (FluidTank) handler;
                fluids.add(new FluidState(tank, tank.getFluid().copy()));
            }
            return new MutableStateSnapshot(mutableItems, itemStacks, fluids);
        }

        private void rollbackOrThrow(RuntimeException originalFailure, Identifier recipeId, String phase) {
            RuntimeException rollbackFailure = null;
            if (items != null) {
                for (int slot = 0; slot < itemStacks.length; slot++) {
                    try {
                        items.setStackInSlot(slot, itemStacks[slot].copy());
                    } catch (RuntimeException failure) {
                        if (rollbackFailure == null) rollbackFailure = failure;
                        else rollbackFailure.addSuppressed(failure);
                    }
                }
            }
            for (FluidState fluid : fluids) {
                try {
                    fluid.tank.setFluid(fluid.stack.copy());
                } catch (RuntimeException failure) {
                    if (rollbackFailure == null) rollbackFailure = failure;
                    else rollbackFailure.addSuppressed(failure);
                }
            }
            if (rollbackFailure != null) {
                throw new RecipeTransactionCorruptedException(
                        "Recipe " + phase + " rollback failed; transaction state is unsafe and must not be retried: " + recipeId,
                        originalFailure,
                        rollbackFailure);
            }
        }
    }

    private record FluidState(FluidTank tank, FluidStack stack) {}

    private static final class VirtualFluidTank {
        private final IFluidHandler handler;
        private final int index;
        private final int capacity;
        private FluidStack fluid;

        private VirtualFluidTank(IFluidHandler handler, int index, int capacity, FluidStack fluid) {
            this.handler = handler;
            this.index = index;
            this.capacity = capacity;
            this.fluid = fluid;
        }
    }
}
