package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Method to check if the inputs (items and fluids) match the recipe requirements.
     * This method ensures that the input slots and tanks contain the required ingredients.
     *
     * @param inputSlots     The item input slots
     * @param inputTanks     The fluid input tanks
     * @param outputSlots    The item cropId slots
     * @param outputTanks    The fluid cropId tanks
     * @return               True if the inputs match the recipe, false otherwise
     */
    public boolean recipeMatch(IItemHandler inputSlots, List<IFluidHandler>
            inputTanks, IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        Map<Item, Integer> itemMap = new HashMap<>();
        Map<Fluid, Integer> fluidMap = new HashMap<>();
        boolean itemsMatch = false;
        boolean fluidsMatch = false;

        // Check if the cropId slots and tanks have enough space for the result
        if (!outputSpaceAvailable(outputSlots, outputTanks)) {
            return false;
        }

        // Validate item ingredients
        if (recipe.getIngredientItems() != null) {
            // Store input items in a map to count quantities
            for (int i = 0; i < inputSlots.getSlots(); i++) {
                ItemStack stack = inputSlots.getStackInSlot(i);
                int newQuantity = itemMap.getOrDefault(stack.getItem(), 0) + stack.getCount();
                itemMap.put(stack.getItem(), newQuantity);
            }
            // Check if the input slots have enough of the required items
            itemsMatch = itemsMatch(recipe.getIngredientItems(), itemMap);
        }

        // Validate fluid ingredients
        if (recipe.getFluidIngredients() != null) {
            // Store input fluids in a map to count amounts
            for (IFluidHandler inputTank : inputTanks) {
                for (int i = 0; i < inputTank.getTanks(); i++) {
                    FluidStack fluidStack = inputTank.getFluidInTank(i);
                    int newAmount = fluidMap.getOrDefault(fluidStack.getFluid(), 0) + fluidStack.getAmount();
                    fluidMap.put(fluidStack.getFluid(), newAmount);
                }
            }
            // Check if the input tanks have enough of the required fluids
            fluidsMatch = fluidsMatch(recipe.getFluidIngredients(), fluidMap);
        }

        return itemsMatch && fluidsMatch;
    }

    /**
     * Helper method to check if input items match the recipe's required items.
     *
     * @param ingredientItems The required item ingredients
     * @param itemMap       The items present in the input slots
     * @return                True if the input items match the required items, false otherwise
     */
    private boolean itemsMatch(IngredientItem[] ingredientItems, Map<Item, Integer> itemMap) {
        Map<Item, Integer> requiredItems = new HashMap<>();
        for (IngredientItem ingredientItem : ingredientItems) {
            ItemStack itemStack = ingredientItem.getItemStack();
            requiredItems.put(itemStack.getItem(), requiredItems.getOrDefault(itemStack.getItem(), 0) + itemStack.getCount());
        }

        for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
            if (!itemMap.containsKey(entry.getKey()) || itemMap.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method to check if input fluids match the recipe's required fluids.
     *
     * @param fluidIngredients The required fluid ingredients
     * @param fluidMap       The fluids present in the input tanks
     * @return                 True if the input fluids match the required fluids, false otherwise
     */
    private boolean fluidsMatch(FluidStack[] fluidIngredients, Map<Fluid, Integer> fluidMap) {
        Map<Fluid, Integer> requiredFluids = new HashMap<>();
        for (FluidStack fluidInput : fluidIngredients) {
            requiredFluids.put(fluidInput.getFluid(), requiredFluids.getOrDefault(fluidInput.getFluid(), 0) + fluidInput.getAmount());
        }

        for (Map.Entry<Fluid, Integer> entry : requiredFluids.entrySet()) {
            if (!fluidMap.containsKey(entry.getKey()) || fluidMap.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the cropId slots and tanks have enough space for the recipe result.
     *
     * @param outputSlots The item cropId slots
     * @param outputTanks The fluid cropId tanks
     * @return            True if there is enough space for the result, false otherwise
     */
    private boolean outputSpaceAvailable(IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        if (outputSlots != null) {
            int availableEmptyItemSlots = 0;
            int availableItemSpace;

            // Calculate the total number of empty slots for items
            for (int i = 0; i < outputSlots.getSlots(); i++) {
                ItemStack slot = outputSlots.getStackInSlot(i);
                if (slot.isEmpty()) {
                    availableEmptyItemSlots++;
                }
            }

            // Check if the item cropId slots have enough space for the result
            for (OutputItem outputItem : recipe.getOutputItems()) {
                availableItemSpace = 0;
                ItemStack itemStack = outputItem.getItemStack();
                int maxStackSize = itemStack.getMaxStackSize();

                // Calculate the available space in non-empty slots for the current result item
                for (int i = 0; i < outputSlots.getSlots(); i++) {
                    ItemStack slot = outputSlots.getStackInSlot(i);
                    if (!slot.isEmpty() && slot.getItem().equals(itemStack.getItem())) {
                        availableItemSpace += (maxStackSize - slot.getCount());
                    }
                }

                // Calculate the total available space including empty slots
                int totalAvailableSpace = availableItemSpace + (availableEmptyItemSlots * maxStackSize);

                // Check if the total available space is sufficient for the result item
                if (totalAvailableSpace < itemStack.getCount()) {
                    return false;
                }

                // Update the remaining empty slots for the next result item
                int remainingCount = itemStack.getCount() - availableItemSpace;
                if (remainingCount > 0) {
                    availableEmptyItemSlots -= (int) Math.ceil((double) remainingCount / maxStackSize);
                }
            }
        }

        if (outputTanks != null) {
            int availableEmptyTankSpace = 0;
            int availableFluidSpace;

            // Calculate the total available empty space for fluids
            for (IFluidHandler outputTank : outputTanks) {
                for (int i = 0; i < outputTank.getTanks(); i++) {
                    FluidStack subFluidStack = outputTank.getFluidInTank(i);
                    if (subFluidStack.isEmpty()) {
                        availableEmptyTankSpace += outputTank.getTankCapacity(i);
                    }
                }
            }

            // Check if the fluid tanks have enough space for the result fluids
            for (FluidStack result : recipe.getFluidOutputs()) {
                availableFluidSpace = 0;

                for (IFluidHandler outputTank : outputTanks) {
                    for (int i = 0; i < outputTank.getTanks(); i++) {
                        FluidStack subFluidStack = outputTank.getFluidInTank(i);
                        if (!subFluidStack.isEmpty() && subFluidStack.getFluid().equals(result.getFluid())) {
                            availableFluidSpace += (outputTank.getTankCapacity(i) - subFluidStack.getAmount());
                        }
                    }
                }

                // Check if the total available space is sufficient for the result fluid
                if (availableEmptyTankSpace + availableFluidSpace < result.getAmount()) {
                    return false;
                }

                // Update the remaining empty tanks for the next result fluid
                int remainingAmount = result.getAmount() - availableFluidSpace;
                if (remainingAmount > 0) {
                    availableEmptyTankSpace -= remainingAmount;
                }
            }
        }

        return true;
    }

    public void assemble(IItemHandler outputSlots, List<IFluidHandler> outputTanks) {
        // Insert the item results into the output slots
        if (recipe.getOutputItems() != null) {
            for (OutputItem outputItem : recipe.getOutputItems()) {
                for (int i = 0; i < outputSlots.getSlots(); i++) {
                    ItemStack slotItemStack = outputSlots.getStackInSlot(i);
                    ItemStack outputItemStack = outputItem.getItemStack();
                    float chance = outputItem.getChance();
                    boolean createItem = true;

                    if (chance < 1.0f) {
                        createItem = Math.random() < chance;
                    }

                    if (slotItemStack.isEmpty() && createItem) {
                        outputSlots.insertItem(i, outputItemStack.copy(), false);
                        break;
                    } else if (slotItemStack.getItem().equals(outputItemStack.getItem())
                            && slotItemStack.getCount() < slotItemStack.getMaxStackSize()
                            && createItem) {
                        slotItemStack.grow(outputItemStack.getCount());
                        break;
                    }
                }
            }
        }

        // Insert the fluid results into the output tanks
        if (recipe.getFluidOutputs() != null) {
            for (FluidStack result : recipe.getFluidOutputs()) {
                for (IFluidHandler outputTank : outputTanks) {
                    FluidStack fluidToFill = result.copy();
                    for (int i = 0; i < outputTank.getTanks(); i++) {
                        FluidStack subFluidStack = outputTank.getFluidInTank(i);
                        if (subFluidStack.isEmpty()) {
                            outputTank.fill(fluidToFill, IFluidHandler.FluidAction.EXECUTE);
                            break;
                        } else if (subFluidStack.getFluid().equals(fluidToFill.getFluid()) && subFluidStack.getAmount() + fluidToFill.getAmount() <= outputTank.getTankCapacity(i)) {
                            outputTank.fill(fluidToFill, IFluidHandler.FluidAction.EXECUTE);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void consumeIngredients(IItemHandler inputSlots, List<IFluidHandler> inputTanks) {
        // Consume the required items
        if (recipe.getIngredientItems() != null) {
            Map<Item, Integer> requiredItemMap = new HashMap<>();
            for (IngredientItem ingredientItem : recipe.getIngredientItems()) {
                ItemStack itemStack = ingredientItem.getItemStack();
                requiredItemMap.put(itemStack.getItem(), requiredItemMap.getOrDefault(itemStack.getItem(), 0) + itemStack.getCount());
            }

            for (Map.Entry<Item, Integer> entry : requiredItemMap.entrySet()) {
                int remaining = entry.getValue();
                for (int i = 0; i < inputSlots.getSlots() && remaining > 0; i++) {
                    ItemStack slotStack = inputSlots.getStackInSlot(i);
                    if (slotStack.getItem().equals(entry.getKey())) {
                        int ingredientIndex = recipe.getIngredientIndex(slotStack.getItem());
                        if (ingredientIndex != -1 && !recipe.getIngredientItems()[ingredientIndex].isConsumable()) {
                            continue;
                        }
                        if (slotStack.getCount() <= remaining) {
                            remaining -= slotStack.getCount();
                            inputSlots.extractItem(i, slotStack.getCount(), false);
                        } else {
                            inputSlots.extractItem(i, remaining, false);
                            remaining = 0;
                        }
                    }
                }
            }
        }

        // Consume the required fluids
        if (recipe.getFluidIngredients() != null) {
            Map<Fluid, Integer> requiredFluidMap = new HashMap<>();
            for (FluidStack fluidInput : recipe.getFluidIngredients()) {
                requiredFluidMap.put(fluidInput.getFluid(), requiredFluidMap.getOrDefault(fluidInput.getFluid(), 0) + fluidInput.getAmount());
            }

            for (Map.Entry<Fluid, Integer> entry : requiredFluidMap.entrySet()) {
                int remaining = entry.getValue();
                for (IFluidHandler inputTank : inputTanks) {
                    if (remaining <= 0) {
                        break;
                    }

                    for (int i = 0; i < inputTank.getTanks() && remaining > 0; i++) {
                        FluidStack tankFluid = inputTank.getFluidInTank(i);
                        if (tankFluid.getFluid().equals(entry.getKey())) {
                            if (tankFluid.getAmount() <= remaining) {
                                remaining -= tankFluid.getAmount();
                                inputTank.drain(tankFluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                            } else {
                                inputTank.drain(remaining, IFluidHandler.FluidAction.EXECUTE);
                                remaining = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the total energy required for this recipe.
     *
     * @return The total energy cost of the recipe
     */
    public int getTotalEnergy() {
        return recipe.getTotalEnergy();
    }

    // Methods not used by BiotechRecipe, overridden to return default values
    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public  ItemStack assemble(@NotNull Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
}
