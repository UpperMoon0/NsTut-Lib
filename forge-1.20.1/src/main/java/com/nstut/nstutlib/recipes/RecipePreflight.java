package com.nstut.nstutlib.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Input-only recipe preflight that deliberately reuses {@link ModRecipe#recipeMatch}
 * with unbounded synthetic outputs. This preserves recipe-specific semantic input
 * matching while deferring real output-capacity checks until the recipe's persisted
 * probabilistic output selection is known.
 */
public final class RecipePreflight {
    private RecipePreflight() {
    }

    public static boolean matchesInputs(ModRecipe<?> recipe,
                                        IItemHandler inputSlots,
                                        List<? extends IFluidHandler> inputTanks) {
        return recipe.recipeMatch(
                inputSlots,
                inputTanks,
                permissiveItemOutput(recipe),
                permissiveFluidOutputs(recipe));
    }

    private static IItemHandler permissiveItemOutput(ModRecipe<?> recipe) {
        if (recipe.getItemOutputs().isEmpty()) {
            return null;
        }

        int slotCount = 0;
        for (OutputItem output : recipe.getItemOutputs()) {
            ItemStack stack = output.getItemStack();
            int maxStackSize = Math.max(1, stack.getMaxStackSize());
            slotCount = Math.addExact(slotCount, Math.max(1, (stack.getCount() + maxStackSize - 1) / maxStackSize));
        }
        final int slots = Math.max(1, slotCount);

        return new IItemHandler() {
            @Override public int getSlots() { return slots; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return ItemStack.EMPTY; }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return Integer.MAX_VALUE; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
        };
    }

    private static List<? extends IFluidHandler> permissiveFluidOutputs(ModRecipe<?> recipe) {
        if (recipe.getFluidOutputs().isEmpty()) {
            return List.of();
        }
        final int tanks = recipe.getFluidOutputs().size();
        return List.of(new IFluidHandler() {
            @Override public int getTanks() { return tanks; }
            @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
            @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }
            @Override public boolean isFluidValid(int tank, FluidStack stack) { return true; }
            @Override public int fill(FluidStack resource, FluidAction action) { return resource.getAmount(); }
            @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
            @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
        });
    }
}
