package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModRecipeTransactionTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void rollsBackPartialItemOutputCommit() {
        ItemStack output = new ItemStack(Items.DIAMOND, 65);
        TestRecipe recipe = recipe(new IngredientItem[0], new OutputItem[] {new OutputItem(output, 1.0f)}, new FluidStack[0], new FluidStack[0]);
        DivergingOutputHandler handler = new DivergingOutputHandler();

        assertThrows(IllegalStateException.class, () -> recipe.assemble(handler, List.of()));
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.getStackInSlot(1).isEmpty());
    }

    @Test
    void rollsBackPartialFluidOutputCommit() {
        TestRecipe recipe = recipe(
                new IngredientItem[0],
                new OutputItem[0],
                new FluidStack[0],
                new FluidStack[] {new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1500)});
        FluidTank first = new FluidTank(1000);
        FluidTank second = new DivergingFluidTank(1000);

        assertThrows(IllegalStateException.class, () -> recipe.assemble(null, List.of(first, second)));
        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
    }

    @Test
    void rollsBackPartialIngredientConsumption() {
        TestRecipe recipe = recipe(
                new IngredientItem[] {
                        new IngredientItem(new ItemStack(Items.DIAMOND), true),
                        new IngredientItem(new ItemStack(Items.GOLD_INGOT), true)
                },
                new OutputItem[0],
                new FluidStack[0],
                new FluidStack[0]);
        DivergingInputHandler handler = new DivergingInputHandler();
        handler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        handler.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT));

        assertThrows(IllegalStateException.class, () -> recipe.tryConsumeIngredients(handler, List.of()));
        assertEquals(1, handler.getStackInSlot(0).getCount());
        assertEquals(1, handler.getStackInSlot(1).getCount());
    }

    private static TestRecipe recipe(IngredientItem[] inputs,
                                     OutputItem[] outputs,
                                     FluidStack[] fluidInputs,
                                     FluidStack[] fluidOutputs) {
        return new TestRecipe(
                new ResourceLocation("nstutlib", "transaction_test"),
                new ModRecipeData(inputs, outputs, fluidInputs, fluidOutputs, 100));
    }

    private static final class TestRecipe extends ModRecipe<TestRecipe> {
        private TestRecipe(ResourceLocation id, ModRecipeData data) {
            super(id, data, null, null);
        }

        @Override
        protected TestRecipe createInstance(ResourceLocation id, ModRecipeData recipeContainer) {
            return new TestRecipe(id, recipeContainer);
        }
    }

    private static final class DivergingOutputHandler extends ItemStackHandler {
        private DivergingOutputHandler() {
            super(2);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && slot == 1) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    }

    private static final class DivergingInputHandler extends ItemStackHandler {
        private DivergingInputHandler() {
            super(2);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate && slot == 1) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    }

    private static final class DivergingFluidTank extends FluidTank {
        private DivergingFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            if (action.execute()) {
                return 0;
            }
            return super.fill(resource, action);
        }
    }
}
