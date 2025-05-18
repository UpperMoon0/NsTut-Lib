package com.nstut.nstutlib.recipes;

import com.nstut.nstutlib.transfer.IFluidStorage;
import com.nstut.nstutlib.transfer.IItemStorage;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public boolean matches(@NotNull Container pInv, @NotNull Level pLevel) {
        // This method is for vanilla compatibility (e.g., recipe book, advancements).
        // It's hard to map directly to IItemStorage/IFluidStorage without a specific
        // container <-> storage mapping.
        // For now, we'll rely on the custom recipeMatch for actual processing.
        return false; // Or true if you want it to always show up, then filter later.
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        // Similar to matches, this is for vanilla crafting grid output.
        // Our recipes are more complex and produce to IItemStorage/IFluidStorage.
        // We return the first item output as a representative item if needed.
        if (recipeData.getOutputItems().length > 0 && recipeData.getOutputItems()[0] != null) {
            return recipeData.getOutputItems()[0].getItemStack().copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        // Most custom machine recipes don't care about crafting grid dimensions.
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        if (recipeData.getOutputItems().length > 0) {
            return recipeData.getOutputItems()[0].getItemStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public @NotNull RecipeType<T> getType() {
        return type;
    }

    public ModRecipeData getRecipeData() {
        return recipeData;
    }

    // TODO: Review and reimplement the logic for Architectury, especially fluid and item handling.
    // The original Forge-specific logic has been commented out or placeholder-ed.

    public boolean recipeMatch(List<IItemStorage> inputItemStorages, List<IFluidStorage> inputFluidStorages, List<IItemStorage> outputItemStorages, List<IFluidStorage> outputFluidStorages) {
        // 1. Check item ingredients
        List<ItemStack> availableItems = new ArrayList<>();
        for (IItemStorage storage : inputItemStorages) {
            for (int slot = 0; slot < storage.getSlotCount(); slot++) {
                ItemStack stackInSlot = storage.getStackInSlot(slot);
                if (!stackInSlot.isEmpty()) {
                    availableItems.add(stackInSlot.copy()); 
                }
            }
        }

        for (IngredientItem ingredient : recipeData.getIngredientItems()) {
            ItemStack requiredStack = ingredient.getItemStack();
            int amountNeeded = requiredStack.getCount();
            boolean ingredientSatisfied = false;

            for (ItemStack availableStack : availableItems) {
                if (availableStack.isEmpty() || amountNeeded <= 0) {
                    if (amountNeeded <= 0) break;
                    continue;
                }
                if (ItemStack.isSameItemSameTags(availableStack, requiredStack)) {
                    int canTake = Math.min(amountNeeded, availableStack.getCount());
                    availableStack.shrink(canTake); // Consume from the copied list for this simulation
                    amountNeeded -= canTake;
                    if (amountNeeded <= 0) {
                        ingredientSatisfied = true;
                        break; 
                    }
                }
            }
            if (!ingredientSatisfied) {
                return false; 
            }
        }

        // 2. Check fluid ingredients
        List<FluidStack> availableFluids = new ArrayList<>();
        for (IFluidStorage storage : inputFluidStorages) {
            for (int tank = 0; tank < storage.getTankCount(); tank++) {
                FluidStack fluidInTank = storage.getFluidInTank(tank);
                if (!fluidInTank.isEmpty()) {
                    availableFluids.add(fluidInTank.copy()); 
                }
            }
        }

        for (IngredientFluid ingredientFluid : recipeData.getFluidIngredients()) { 
            FluidStack requiredFluid = ingredientFluid.getFluidStack(); 
            long amountNeeded = requiredFluid.getAmount();
            boolean ingredientSatisfied = false;

            for (FluidStack availableStack : availableFluids) {
                if (availableStack.isEmpty() || amountNeeded <= 0) {
                    if (amountNeeded <= 0) break;
                    continue;
                }
                // Compare fluid type and NBT data
                if (availableStack.getFluid().isSame(requiredFluid.getFluid()) && java.util.Objects.equals(availableStack.getTag(), requiredFluid.getTag())) {
                    long canTake = Math.min(amountNeeded, availableStack.getAmount());
                    availableStack.setAmount(availableStack.getAmount() - canTake); 
                    amountNeeded -= canTake;
                    if (amountNeeded <= 0) {
                        ingredientSatisfied = true;
                        break; 
                    }
                }
            }
            if (!ingredientSatisfied) {
                return false; 
            }
        }

        // 3. Check output space
        if (!outputSpaceAvailable(outputItemStorages, outputFluidStorages)) {
            return false;
        }

        return true; // All checks passed
    }

    private boolean outputSpaceAvailable(List<IItemStorage> outputItemStorages, List<IFluidStorage> outputFluidStorages) {
        // Check item output space
        for (OutputItem outputItem : recipeData.getOutputItems()) {
            ItemStack itemStackToOutput = outputItem.getItemStack();
            if (itemStackToOutput.isEmpty()) {
                continue;
            }
            // Consider chance for output space check?
            // The original Forge code's outputSpaceAvailable doesn't seem to consider chance,
            // it checks if there's space for ALL potential outputs.
            // Let's assume worst-case (all items with chance > 0 are produced) for space check.
            // However, the current implementation of outputSpaceAvailable already iterates through getOutputItems.

            ItemStack remaining = itemStackToOutput.copy();

            for (IItemStorage itemStorage : outputItemStorages) {
                if (remaining.isEmpty()) break;
                ItemStack insertedSimulated = itemStorage.insert(remaining.copy(), true); // Simulate insert
                remaining.setCount(insertedSimulated.getCount()); // Update remaining based on what couldn't be inserted
            }
            if (!remaining.isEmpty()) {
                return false; // Not enough space for this item output
            }
        }

        // Check fluid output space
        for (OutputFluid outputFluid : recipeData.getFluidOutputs()) { // Changed here
            FluidStack fluidStackToOutput = outputFluid.getFluidStack(); // Added here
            if (fluidStackToOutput.isEmpty()) {
                continue;
            }
            FluidStack remainingFluid = fluidStackToOutput.copy();
            for (IFluidStorage fluidStorage : outputFluidStorages) {
                if (remainingFluid.isEmpty()) break;
                long insertedAmountSimulated = fluidStorage.insert(remainingFluid.copy(), true); // Simulate insert
                remainingFluid.setAmount(remainingFluid.getAmount() - insertedAmountSimulated);
            }
            if (!remainingFluid.isEmpty()) {
                return false; // Not enough space for this fluid output
            }
        }
        return true;
    }

    public void assemble(List<IItemStorage> outputItemStorages, List<IFluidStorage> outputFluidStorages) {
        // Assemble Item Outputs
        if (recipeData.getOutputItems() != null) {
            for (OutputItem outputItem : recipeData.getOutputItems()) {
                if (outputItem == null || outputItem.getItemStack().isEmpty()) {
                    continue;
                }
                ItemStack itemToProduce = outputItem.getItemStack();
                float chance = outputItem.getChance();
                if (Math.random() < chance) { 
                    ItemStack remainingToOutput = itemToProduce.copy();
                    for (IItemStorage storage : outputItemStorages) {
                        if (remainingToOutput.isEmpty()) {
                            break; 
                        }
                        // Pass false for simulate as this is actual assembly
                        remainingToOutput = storage.insert(remainingToOutput, false); 
                    }
                }
            }
        }

        // Assemble Fluid Outputs
        if (recipeData.getFluidOutputs() != null) {
            for (OutputFluid outputFluid : recipeData.getFluidOutputs()) { 
                if (outputFluid == null || outputFluid.getFluidStack().isEmpty()) {
                    continue;
                }
                FluidStack fluidToProduce = outputFluid.getFluidStack(); // Get FluidStack from OutputFluid
                if (Math.random() < outputFluid.getChance()) {
                    FluidStack remainingToOutput = fluidToProduce.copy();
                    for (IFluidStorage storage : outputFluidStorages) {
                        if (remainingToOutput.isEmpty()) {
                            break; 
                        }
                        // Pass false for simulate
                        long insertedAmount = storage.insert(remainingToOutput.copy(), false); // Pass copy to avoid modification by insert
                        remainingToOutput.setAmount(remainingToOutput.getAmount() - insertedAmount);
                    }
                }
            }
        }
    }

    public void consumeIngredients(List<IItemStorage> inputItemStorages, List<IFluidStorage> inputFluidStorages) {
        // Consume Item Ingredients
        if (recipeData.getIngredientItems() != null) {
            for (IngredientItem ingredient : recipeData.getIngredientItems()) {
                if (ingredient == null || !ingredient.isConsumable()) {
                    continue;
                }
                ItemStack requiredStack = ingredient.getItemStack();
                int amountToConsume = requiredStack.getCount();

                for (IItemStorage storage : inputItemStorages) {
                    if (amountToConsume <= 0) {
                        break; 
                    }
                    for (int i = 0; i < storage.getSlotCount(); i++) {
                        if (amountToConsume <= 0) break; 
                        ItemStack stackInSlot = storage.getStackInSlot(i);
                        // Match item type and NBT.
                        if (ItemStack.isSameItemSameTags(stackInSlot, requiredStack)) {
                             // Pass false for simulate as this is actual consumption
                            ItemStack extracted = storage.extract(i, amountToConsume, false);
                            amountToConsume -= extracted.getCount();
                        }
                    }
                }
            }
        }

        // Consume Fluid Ingredients
        if (recipeData.getFluidIngredients() != null) {
            for (IngredientFluid ingredientFluid : recipeData.getFluidIngredients()) {
                if (ingredientFluid == null || !ingredientFluid.isConsumable()) {
                    continue;
                }
                FluidStack requiredFluid = ingredientFluid.getFluidStack();
                long amountToConsume = requiredFluid.getAmount();

                for (IFluidStorage fluidStorage : inputFluidStorages) {
                    if (amountToConsume <= 0) {
                        break; 
                    }
                    // Iterate through tanks to find matching fluid and extract
                    for (int tank = 0; tank < fluidStorage.getTankCount(); tank++) {
                        if (amountToConsume <= 0) break;
                        FluidStack fluidInTank = fluidStorage.getFluidInTank(tank);
                        // Compare fluid type and NBT data
                        if (fluidInTank.getFluid().isSame(requiredFluid.getFluid()) && java.util.Objects.equals(fluidInTank.getTag(), requiredFluid.getTag())) {
                            long toExtract = Math.min(amountToConsume, fluidInTank.getAmount());
                            if (toExtract > 0) {
                                FluidStack extracted = fluidStorage.extract(toExtract, false);
                                // Verify the extracted fluid is what was expected.
                                // Compare fluid type and NBT data
                                if (extracted.getFluid().isSame(requiredFluid.getFluid()) && java.util.Objects.equals(extracted.getTag(), requiredFluid.getTag()) && extracted.getAmount() <= toExtract) {
                                    amountToConsume -= extracted.getAmount();
                                } else if (!extracted.isEmpty()){
                                    // Log error and attempt to put back if wrong fluid extracted.
                                    System.err.println("ModRecipe: Extracted incorrect fluid or amount during consumption. Required: " + requiredFluid + " (amount " + toExtract + "), Got: " + extracted + ". Attempting to re-insert.");
                                    long reInserted = fluidStorage.insert(extracted, false);
                                    if (reInserted != extracted.getAmount()) {
                                        System.err.println("ModRecipe: Failed to fully re-insert incorrectly extracted fluid. Discrepancy: " + (extracted.getAmount() - reInserted));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<IngredientItem> getItemInputs() {
        return Arrays.asList(recipeData.getIngredientItems());
    }

    public List<OutputItem> getItemOutputs() {
        return Arrays.asList(recipeData.getOutputItems());
    }

    public List<IngredientFluid> getFluidInputs() { // Changed return type
        return Arrays.asList(recipeData.getFluidIngredients());
    }

    public List<OutputFluid> getFluidOutputs() { // Changed return type
        return Arrays.asList(recipeData.getFluidOutputs());
    }

    public int getTotalEnergy() {
        return recipeData.getTotalEnergy();
    }
}
