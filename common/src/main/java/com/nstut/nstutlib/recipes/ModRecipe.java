package com.nstut.nstutlib.recipes;

import com.nstut.nstutlib.blocks.MachineBlockEntity;
import dev.architectury.fluid.FluidStack; // Replaced Forge FluidStack
import dev.architectury.transfer.transfer.TransferAction; // Corrected import
import dev.architectury.transfer.transfer.item.ItemTransfer; // Corrected import
import dev.architectury.transfer.transfer.fluid.FluidTransfer; // Corrected import
import dev.architectury.transfer.transfer.fluid.FluidHandler; // Added import
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
import org.jetbrains.annotations.NotNull; // Added import

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ModRecipe<T extends ModRecipe<T>> implements Recipe<Container>, RecipeFactory<T> {
    protected final ResourceLocation id;
    protected final ModRecipeData recipeData;
    private final RecipeSerializer<T> serializer;
    private final RecipeType<T> type;

    protected ModRecipe(ResourceLocation id, ModRecipeData recipeData, RecipeSerializer<T> serializer, RecipeType<T> type) {
        this.id = id;
        this.recipeData = recipeData;
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
    public RecipeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<T> getType() {
        return type;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public List<IngredientItem> getItemIngredients() {
        return List.of(recipeData.getIngredientItems());
    }

    public List<FluidStack> getFluidIngredients() {
        return List.of(recipeData.getFluidIngredients());
    }

    public List<OutputItem> getItemOutputs() {
        return List.of(recipeData.getOutputItems());
    }

    public List<FluidStack> getFluidOutputs() {
        return List.of(recipeData.getFluidOutputs());
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
    public boolean recipeMatch(Container inputSlots, List<FluidHandler> inputTanks, Container outputSlots, List<FluidHandler> outputTanks) {
        boolean itemsMatch = itemIngredientsMatch(inputSlots);
        boolean fluidsMatch = fluidIngredientsMatch(inputTanks);
        boolean outputSpace = outputSpaceAvailable(outputSlots, outputTanks);

        return itemsMatch && fluidsMatch && outputSpace;
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
    private boolean fluidsMatch(dev.architectury.fluid.FluidStack[] fluidIngredients, Map<Fluid, Integer> fluidMap) {
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
    private boolean outputSpaceAvailable(Container outputSlots, List<FluidHandler> outputTanks) {
        boolean itemOutputSpace = true;
        boolean fluidOutputSpace = true;

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
            for (OutputItem outputItem : recipeData.getOutputItems()) {
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
                    itemOutputSpace = false;
                    break;
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
            for (FluidHandler outputTank : outputTanks) {
                for (int i = 0; i < outputTank.getTankCount(); i++) { // Corrected method call
                    FluidStack subFluidStack = outputTank.getFluidInTank(i);
                    if (subFluidStack.isEmpty()) {
                        availableEmptyTankSpace += outputTank.getTankCapacity(i);
                    }
                }
            }

            // Check if the fluid tanks have enough space for the result fluids
            for (FluidStack result : recipeData.getFluidOutputs()) {
                availableFluidSpace = 0;

                for (FluidHandler outputTank : outputTanks) {
                    for (int i = 0; i < outputTank.getTankCount(); i++) { // Corrected method call
                        FluidStack subFluidStack = outputTank.getFluidInTank(i);
                        if (!subFluidStack.isEmpty() && subFluidStack.getFluid().equals(result.getFluid())) {
                            availableFluidSpace += (outputTank.getTankCapacity(i) - subFluidStack.getAmount());
                        }
                    }
                }

                // Check if the total available space is sufficient for the result fluid
                if (availableEmptyTankSpace + availableFluidSpace < result.getAmount()) {
                    fluidOutputSpace = false;
                    break;
                }

                // Update the remaining empty tanks for the next result fluid
                int remainingAmount = result.getAmount() - availableFluidSpace;
                if (remainingAmount > 0) {
                    availableEmptyTankSpace -= remainingAmount;
                }
            }
        }

        return itemOutputSpace && fluidOutputSpace;
    }

    public void assemble(Container outputSlots, List<FluidHandler> outputTanks) {
        for (int i = 0; i < recipeData.getOutputItems().length; i++) {
            OutputItem outputItem = recipeData.getOutputItems()[i];
            for (int j = 0; j < outputSlots.getSlots(); j++) {
                ItemStack slotItemStack = outputSlots.getStackInSlot(j);
                ItemStack outputItemStack = outputItem.getItemStack();
                float chance = outputItem.getChance();
                boolean createItem = true;

                if (chance < 1.0f) {
                    createItem = Math.random() < chance;
                }

                if (slotItemStack.isEmpty() && createItem) {
                    ItemTransfer.insertItem(outputSlots, outputItemStack.copy(), j);
                    break;
                } else if (slotItemStack.getItem().equals(outputItemStack.getItem())
                        && slotItemStack.getCount() < slotItemStack.getMaxStackSize()
                        && createItem) {
                    slotItemStack.grow(outputItemStack.getCount());
                    break;
                }
            }
        }

        // Insert the fluid results into the output tanks
        if (recipeData.getFluidOutputs() != null) {
            for (FluidStack result : recipeData.getFluidOutputs()) {
                for (FluidHandler outputTank : outputTanks) {
                    FluidStack fluidToFill = result.copy();
                    for (int i = 0; i < outputTank.getTankCount(); i++) { // Corrected method call
                        FluidStack subFluidStack = outputTank.getFluidInTank(i);
                        if (subFluidStack.isEmpty()) {
                            outputTank.fill(fluidToFill, TransferAction.EXECUTE); // Corrected method call
                            break;
                        } else if (subFluidStack.getFluid().equals(fluidToFill.getFluid()) && subFluidStack.getAmount() + fluidToFill.getAmount() <= outputTank.getTankCapacity(i)) {
                            outputTank.fill(fluidToFill, TransferAction.EXECUTE); // Corrected method call
                            break;
                        }
                    }
                }
            }
        }
    }

    public void consumeIngredients(Container inputSlots, List<FluidHandler> inputTanks) {
        // Consume items
        for (IngredientItem ingredientItem : recipeData.getIngredientItems()) {
            ItemStack itemStack = ingredientItem.getItemStack();
            for (int i = 0; i < inputSlots.getSlots(); i++) {
                ItemStack slotStack = inputSlots.getStackInSlot(i);
                if (slotStack.getItem().equals(itemStack.getItem())) {
                    int ingredientIndex = recipeData.getIngredientIndex(slotStack.getItem());
                    if (ingredientIndex != -1 && !recipeData.getIngredientItems()[ingredientIndex].isConsumable()) {
                        continue;
                    }
                    if (slotStack.getCount() <= itemStack.getCount()) {
                        itemStack.grow(-slotStack.getCount());
                        inputSlots.extractItem(i, slotStack.getCount(), false);
                    } else {
                        inputSlots.extractItem(i, itemStack.getCount(), false);
                        itemStack.setCount(0);
                        break;
                    }
                }
            }
        }

        // Consume the required fluids
        if (recipeData.getFluidIngredients() != null) {
            Map<Fluid, Integer> requiredFluidMap = new HashMap<>();
            for (FluidStack fluidInput : recipeData.getFluidIngredients()) {
                requiredFluidMap.put(fluidInput.getFluid(), requiredFluidMap.getOrDefault(fluidInput.getFluid(), 0) + fluidInput.getAmount());
            }

            for (Map.Entry<Fluid, Integer> entry : requiredFluidMap.entrySet()) {
                int remaining = entry.getValue();
                for (FluidHandler inputTank : inputTanks) {
                    if (remaining <= 0) {
                        break;
                    }

                    for (int i = 0; i < inputTank.getTankCount() && remaining > 0; i++) { // Corrected method call
                        FluidStack tankFluid = inputTank.getFluidInTank(i);
                        if (tankFluid.getFluid().equals(entry.getKey())) {
                            if (tankFluid.getAmount() <= remaining) {
                                remaining -= tankFluid.getAmount();
                                inputTank.drain(tankFluid.copy().withAmount(tankFluid.getAmount()), TransferAction.EXECUTE); // Corrected method call
                            } else {
                                inputTank.drain(tankFluid.copy().withAmount(remaining), TransferAction.EXECUTE); // Corrected method call
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
        return recipeData.getTotalEnergy();
    }

    // Methods not used by BiotechRecipe, overridden to return default values
    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public  ItemStack assemble(@NotNull Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY; // Return empty item stack instead of null
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY; // Return empty item stack instead of null
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
}
