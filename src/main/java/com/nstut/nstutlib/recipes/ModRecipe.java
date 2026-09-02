package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ModRecipe<T extends ModRecipe<T>> implements Recipe<Container>, RecipeFactory<T> {
    protected final ResourceLocation id;
    @Getter
    protected final ModRecipeData recipe;
    private final RecipeSerializer<T> serializer;
    private final RecipeType<T> type;

    protected ModRecipe(ResourceLocation id, ModRecipeData recipe, RecipeSerializer<T> serializer, RecipeType<T> type) {
        this.id = id;
        this.recipe = recipe;
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public T create(ResourceLocation id, ModRecipeData recipeData) {
        if (recipeData == null) {
            throw new IllegalArgumentException("Recipe data cannot be null");
        }
        return createInstance(id, recipeData);
    }

    protected abstract T createInstance(ResourceLocation id, ModRecipeData recipeContainer);

    @Override
    public @NotNull RecipeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public @NotNull RecipeType<T> getType() {
        return type;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    public List<IngredientItem> getItemIngredients() {
        return List.of(recipe.getIngredientItems());
    }

    public List<FluidStack> getFluidIngredients() {
        return List.of(recipe.getFluidIngredients());
    }

    public List<OutputItem> getItemOutputs() {
        return List.of(recipe.getOutputItems());
    }

    public List<FluidStack> getFluidOutputs() {
        return List.of(recipe.getFluidOutputs());
    }

    public boolean recipeMatch(IItemHandler inputSlots,
                               List<IFluidHandler> inputTanks,
                               IItemHandler outputSlots,
                               List<IFluidHandler> outputTanks) {
        return hasRequiredItems(inputSlots)
                && hasRequiredFluids(inputTanks)
                && canFitOutputs(outputSlots, outputTanks);
    }

    private boolean hasRequiredItems(IItemHandler inputSlots) {
        IngredientItem[] ingredients = recipe.getIngredientItems();
        if (ingredients.length == 0) {
            return true;
        }
        if (inputSlots == null) {
            return false;
        }

        for (ItemStack required : aggregateRequiredItemStacks()) {
            int remaining = required.getCount();
            for (int slot = 0; slot < inputSlots.getSlots() && remaining > 0; slot++) {
                ItemStack present = inputSlots.getStackInSlot(slot);
                if (ItemStack.isSameItemSameTags(required, present)) {
                    remaining -= Math.min(remaining, present.getCount());
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private List<ItemStack> aggregateRequiredItemStacks() {
        List<ItemStack> requiredStacks = new ArrayList<>();
        for (IngredientItem ingredient : recipe.getIngredientItems()) {
            ItemStack ingredientStack = ingredient.getItemStack();
            ItemStack aggregate = requiredStacks.stream()
                    .filter(existing -> ItemStack.isSameItemSameTags(existing, ingredientStack))
                    .findFirst()
                    .orElse(null);
            if (aggregate == null) {
                requiredStacks.add(ingredientStack.copy());
            } else {
                aggregate.setCount(aggregate.getCount() + ingredientStack.getCount());
            }
        }
        return requiredStacks;
    }

    private boolean hasRequiredFluids(List<IFluidHandler> inputTanks) {
        FluidStack[] ingredients = recipe.getFluidIngredients();
        if (ingredients.length == 0) {
            return true;
        }
        if (inputTanks == null || inputTanks.isEmpty()) {
            return false;
        }

        for (FluidStack required : aggregateRequiredFluidStacks()) {
            int remaining = required.getAmount();
            for (IFluidHandler handler : inputTanks) {
                for (int tank = 0; tank < handler.getTanks() && remaining > 0; tank++) {
                    FluidStack present = handler.getFluidInTank(tank);
                    if (sameFluid(required, present)) {
                        remaining -= Math.min(remaining, present.getAmount());
                    }
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private List<FluidStack> aggregateRequiredFluidStacks() {
        List<FluidStack> requiredStacks = new ArrayList<>();
        for (FluidStack ingredient : recipe.getFluidIngredients()) {
            FluidStack aggregate = requiredStacks.stream()
                    .filter(existing -> sameFluid(existing, ingredient))
                    .findFirst()
                    .orElse(null);
            if (aggregate == null) {
                requiredStacks.add(ingredient.copy());
            } else {
                aggregate.grow(ingredient.getAmount());
            }
        }
        return requiredStacks;
    }

    public boolean canFitOutputs(IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        return itemOutputsFit(outputSlots) && fluidOutputsFit(outputTanks);
    }

    private boolean itemOutputsFit(IItemHandler outputSlots) {
        OutputItem[] outputs = recipe.getOutputItems();
        if (outputs.length == 0) {
            return true;
        }
        if (outputSlots == null) {
            return false;
        }

        ItemStack[] virtualSlots = new ItemStack[outputSlots.getSlots()];
        for (int slot = 0; slot < outputSlots.getSlots(); slot++) {
            virtualSlots[slot] = outputSlots.getStackInSlot(slot).copy();
        }

        for (OutputItem output : outputs) {
            ItemStack remaining = output.getItemStack().copy();
            for (int slot = 0; slot < virtualSlots.length && !remaining.isEmpty(); slot++) {
                if (!outputSlots.isItemValid(slot, remaining)) {
                    continue;
                }

                ItemStack present = virtualSlots[slot];
                int slotLimit = Math.min(outputSlots.getSlotLimit(slot), remaining.getMaxStackSize());
                if (present.isEmpty()) {
                    int moved = Math.min(slotLimit, remaining.getCount());
                    ItemStack inserted = remaining.copy();
                    inserted.setCount(moved);
                    virtualSlots[slot] = inserted;
                    remaining.shrink(moved);
                } else if (ItemStack.isSameItemSameTags(present, remaining)) {
                    int max = Math.min(outputSlots.getSlotLimit(slot), present.getMaxStackSize());
                    int moved = Math.min(Math.max(0, max - present.getCount()), remaining.getCount());
                    if (moved > 0) {
                        present.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean fluidOutputsFit(List<IFluidHandler> outputTanks) {
        FluidStack[] outputs = recipe.getFluidOutputs();
        if (outputs.length == 0) {
            return true;
        }
        if (outputTanks == null || outputTanks.isEmpty()) {
            return false;
        }

        List<VirtualFluidTank> virtualTanks = new ArrayList<>();
        for (IFluidHandler handler : outputTanks) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                virtualTanks.add(new VirtualFluidTank(
                        handler,
                        tank,
                        handler.getTankCapacity(tank),
                        handler.getFluidInTank(tank).copy()));
            }
        }

        for (FluidStack output : outputs) {
            FluidStack remaining = output.copy();
            for (VirtualFluidTank tank : virtualTanks) {
                if (remaining.isEmpty()) {
                    break;
                }
                if (tank.fluid.isEmpty()) {
                    if (!tank.handler.isFluidValid(tank.index, remaining)) {
                        continue;
                    }
                    int moved = Math.min(tank.capacity, remaining.getAmount());
                    tank.fluid = remaining.copy();
                    tank.fluid.setAmount(moved);
                    remaining.shrink(moved);
                } else if (sameFluid(tank.fluid, remaining)) {
                    int moved = Math.min(Math.max(0, tank.capacity - tank.fluid.getAmount()), remaining.getAmount());
                    if (moved > 0) {
                        tank.fluid.grow(moved);
                        remaining.shrink(moved);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void assemble(IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        if (!canFitOutputs(outputSlots, outputTanks)) {
            throw new RecipeTransactionException("Recipe outputs no longer fit: " + id);
        }
        MutableStateSnapshot snapshot = MutableStateSnapshot.capture(outputSlots, outputTanks, id, "output");
        try {
            assembleUnchecked(outputSlots, outputTanks);
        } catch (RuntimeException failure) {
            snapshot.rollback(failure);
            throw new RecipeTransactionException("Recipe output transaction rolled back: " + id, failure);
        }
    }

    private void assembleUnchecked(IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        if (outputSlots != null) {
            for (OutputItem output : recipe.getOutputItems()) {
                float chance = output.getChance();
                if (chance < 1.0f && ThreadLocalRandom.current().nextFloat() >= chance) {
                    continue;
                }

                ItemStack remaining = output.getItemStack().copy();
                for (int slot = 0; slot < outputSlots.getSlots() && !remaining.isEmpty(); slot++) {
                    remaining = outputSlots.insertItem(slot, remaining, false);
                }
                if (!remaining.isEmpty()) {
                    throw new RecipeTransactionException("Output inventory changed during recipe commit: " + id);
                }
            }
        }

        for (FluidStack output : recipe.getFluidOutputs()) {
            FluidStack remaining = output.copy();
            for (IFluidHandler handler : safeFluidHandlers(outputTanks)) {
                if (remaining.isEmpty()) {
                    break;
                }
                int accepted = handler.fill(remaining, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) {
                    continue;
                }
                FluidStack portion = remaining.copy();
                portion.setAmount(Math.min(accepted, remaining.getAmount()));
                int filled = handler.fill(portion, IFluidHandler.FluidAction.EXECUTE);
                if (filled != portion.getAmount()) {
                    throw new RecipeTransactionException("Fluid output handler diverged during recipe commit: " + id);
                }
                remaining.shrink(filled);
            }
            if (!remaining.isEmpty()) {
                throw new RecipeTransactionException("Output fluid handlers changed during recipe commit: " + id);
            }
        }
    }

    public boolean tryConsumeIngredients(IItemHandler inputSlots, List<IFluidHandler> inputTanks) {
        if (!hasRequiredItems(inputSlots) || !hasRequiredFluids(inputTanks)) {
            return false;
        }

        MutableStateSnapshot snapshot = MutableStateSnapshot.capture(inputSlots, inputTanks, id, "input");
        try {
            consumeIngredientsUnchecked(inputSlots, inputTanks);
            return true;
        } catch (RuntimeException failure) {
            snapshot.rollback(failure);
            throw new RecipeTransactionException("Recipe input transaction rolled back: " + id, failure);
        }
    }

    private void consumeIngredientsUnchecked(IItemHandler inputSlots, List<IFluidHandler> inputTanks) {
        for (IngredientItem ingredient : recipe.getIngredientItems()) {
            if (!ingredient.isConsumable()) {
                continue;
            }
            ItemStack required = ingredient.getItemStack();
            int remaining = required.getCount();
            for (int slot = 0; slot < inputSlots.getSlots() && remaining > 0; slot++) {
                ItemStack present = inputSlots.getStackInSlot(slot);
                if (!ItemStack.isSameItemSameTags(required, present)) {
                    continue;
                }
                ItemStack extracted = inputSlots.extractItem(slot, remaining, false);
                if (!extracted.isEmpty() && !ItemStack.isSameItemSameTags(required, extracted)) {
                    throw new RecipeTransactionException("Item input handler returned the wrong stack during recipe commit: " + id);
                }
                remaining -= extracted.getCount();
            }
            if (remaining > 0) {
                throw new RecipeTransactionException("Item input handler changed during recipe commit: " + id);
            }
        }

        for (FluidStack ingredient : recipe.getFluidIngredients()) {
            int remaining = ingredient.getAmount();
            for (IFluidHandler handler : safeFluidHandlers(inputTanks)) {
                if (remaining <= 0) {
                    break;
                }
                FluidStack request = ingredient.copy();
                request.setAmount(remaining);
                FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty() && !sameFluid(ingredient, drained)) {
                    throw new RecipeTransactionException("Fluid input handler returned the wrong fluid during recipe commit: " + id);
                }
                if (sameFluid(ingredient, drained)) {
                    remaining -= drained.getAmount();
                }
            }
            if (remaining > 0) {
                throw new RecipeTransactionException("Fluid input handler changed during recipe commit: " + id);
            }
        }
    }

    public void consumeIngredients(IItemHandler inputSlots, List<IFluidHandler> inputTanks) {
        if (!tryConsumeIngredients(inputSlots, inputTanks)) {
            throw new RecipeTransactionException("Recipe inputs are no longer available: " + id);
        }
    }

    private static List<IFluidHandler> safeFluidHandlers(List<IFluidHandler> handlers) {
        return handlers == null ? Collections.emptyList() : handlers;
    }

    private static boolean sameFluid(FluidStack first, FluidStack second) {
        return !first.isEmpty() && !second.isEmpty() && first.isFluidEqual(second);
    }

    public int getTotalEnergy() {
        return recipe.getTotalEnergy();
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    private static final class MutableStateSnapshot {
        private final IItemHandlerModifiable items;
        private final ItemStack[] itemStacks;
        private final List<FluidState> fluids;

        private MutableStateSnapshot(IItemHandlerModifiable items, ItemStack[] itemStacks, List<FluidState> fluids) {
            this.items = items;
            this.itemStacks = itemStacks;
            this.fluids = fluids;
        }

        private static MutableStateSnapshot capture(IItemHandler itemHandler,
                                                    List<IFluidHandler> fluidHandlers,
                                                    ResourceLocation recipeId,
                                                    String phase) {
            IItemHandlerModifiable mutableItems = null;
            ItemStack[] itemStacks = new ItemStack[0];
            if (itemHandler != null) {
                if (!(itemHandler instanceof IItemHandlerModifiable modifiable)) {
                    throw new RecipeTransactionException("Transactional " + phase + " item handler is not restorable for recipe " + recipeId);
                }
                mutableItems = modifiable;
                itemStacks = new ItemStack[itemHandler.getSlots()];
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    itemStacks[slot] = itemHandler.getStackInSlot(slot).copy();
                }
            }

            List<FluidState> fluids = new ArrayList<>();
            for (IFluidHandler handler : safeFluidHandlers(fluidHandlers)) {
                if (!(handler instanceof FluidTank tank)) {
                    throw new RecipeTransactionException("Transactional " + phase + " fluid handler is not restorable for recipe " + recipeId);
                }
                fluids.add(new FluidState(tank, tank.getFluid().copy()));
            }
            return new MutableStateSnapshot(mutableItems, itemStacks, fluids);
        }

        private void rollback(RuntimeException originalFailure) {
            try {
                if (items != null) {
                    for (int slot = 0; slot < itemStacks.length; slot++) {
                        items.setStackInSlot(slot, itemStacks[slot].copy());
                    }
                }
                for (FluidState fluid : fluids) {
                    fluid.tank.setFluid(fluid.stack.copy());
                }
            } catch (RuntimeException rollbackFailure) {
                originalFailure.addSuppressed(rollbackFailure);
            }
        }
    }

    private record FluidState(FluidTank tank, FluidStack stack) {
    }

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
