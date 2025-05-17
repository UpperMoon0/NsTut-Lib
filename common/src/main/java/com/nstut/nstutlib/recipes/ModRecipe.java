package com.nstut.nstutlib.recipes;

import com.nstut.nstutlib.blocks.MachineBlockEntity;
import dev.architectury.fluid.FluidStack;
import dev.architectury.transfer.fluid.FluidStorage;
import dev.architectury.transfer.item.ItemStorage;
import dev.architectury.transfer.transaction.TransactionContext;
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
import org.jetbrains.annotations.NotNull;

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
    public boolean matches(@NotNull Container pInv, @NotNull Level pLevel) {
        return false; // Placeholder
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY; // Placeholder
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true; // Placeholder
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY; // Placeholder
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

    // public boolean recipeMatch(Container inputSlots, List<FluidStorage> inputTanks, Container outputSlots, List<FluidStorage> outputTanks) {
    //     // ... (original logic commented out)
    //     return false;
    // }

    // private boolean outputSpaceAvailable(Container outputSlots, List<FluidStorage> outputTanks) {
    //     // ... (original logic commented out)
    //     return false;
    // }

    // public void assemble(Container outputSlots, List<FluidStorage> outputTanks) {
    //     // ... (original logic commented out)
    // }

    // public void consumeIngredients(Container inputSlots, List<FluidStorage> inputTanks) {
    //     // ... (original logic commented out)
    // }

    public List<ItemStack> getItemInputs() {
        return recipeData.getItemInputs().stream()
                .map(IngredientItem::getItemStack)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<ItemStack> getItemOutputs() {
        return recipeData.getItemOutputs().stream()
                .map(OutputItem::getItemStack)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<FluidStack> getFluidInputs() {
        return recipeData.getFluidInputs().stream()
                .map(IngredientFluid::getFluidStack)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<FluidStack> getFluidOutputs() {
        return recipeData.getFluidOutputs().stream()
                .map(OutputFluid::getFluidStack)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
