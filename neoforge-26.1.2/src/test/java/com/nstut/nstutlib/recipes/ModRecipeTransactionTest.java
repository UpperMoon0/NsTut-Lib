package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
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
        RegistryAccess.Frozen builtIns = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(builtIns).forEach(pending -> pending.apply());
    }

    @Test
    void rollsBackPartialItemOutputCommit() {
        TestRecipe recipe = recipe(new IngredientItem[0], new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 65), 1.0f)}, new FluidStack[0], new FluidStack[0]);
        DivergingOutputHandler handler = new DivergingOutputHandler();
        assertThrows(RecipeTransactionException.class, () -> recipe.assemble(handler, List.of()));
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.getStackInSlot(1).isEmpty());
    }

    @Test
    void rollsBackPartialFluidOutputCommit() {
        TestRecipe recipe = recipe(new IngredientItem[0], new OutputItem[0], new FluidStack[0],
                new FluidStack[] {new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1500)});
        FluidTank first = new FluidTank(1000);
        FluidTank second = new DivergingFluidTank(1000);
        assertThrows(RecipeTransactionException.class, () -> recipe.assemble(null, List.of(first, second)));
        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
    }

    @Test
    void rollsBackPartialIngredientConsumption() {
        TestRecipe recipe = recipe(
                new IngredientItem[] {
                        new IngredientItem(new ItemStack(Items.DIAMOND), true),
                        new IngredientItem(new ItemStack(Items.GOLD_INGOT), true)
                }, new OutputItem[0], new FluidStack[0], new FluidStack[0]);
        DivergingInputHandler handler = new DivergingInputHandler();
        handler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        handler.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT));
        assertThrows(RecipeTransactionException.class, () -> recipe.tryConsumeIngredients(handler, List.of()));
        assertEquals(1, handler.getStackInSlot(0).getCount());
        assertEquals(1, handler.getStackInSlot(1).getCount());
    }

    @Test
    void rolledBackOutputCanBeRetriedSafely() {
        TestRecipe recipe = recipe(new IngredientItem[0], new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 65), 1.0f)}, new FluidStack[0], new FluidStack[0]);
        OneShotDivergingOutputHandler handler = new OneShotDivergingOutputHandler();
        assertThrows(RecipeTransactionException.class, () -> recipe.assemble(handler, List.of()));
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.getStackInSlot(1).isEmpty());
        recipe.assemble(handler, List.of());
        assertEquals(64, handler.getStackInSlot(0).getCount());
        assertEquals(1, handler.getStackInSlot(1).getCount());
    }

    @Test
    void retryUsesTheSamePersistedChanceOutputSelection() {
        TestRecipe recipe = recipe(new IngredientItem[0],
                new OutputItem[] {
                        new OutputItem(new ItemStack(Items.DIAMOND, 65), 0.5f),
                        new OutputItem(new ItemStack(Items.EMERALD, 65), 0.5f)
                }, new FluidStack[0], new FluidStack[0]);
        OneShotDivergingOutputHandler handler = new OneShotDivergingOutputHandler();
        int[] persistedSelection = {1};
        assertThrows(RecipeTransactionException.class, () -> recipe.assemble(handler, List.of(), persistedSelection));
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.getStackInSlot(1).isEmpty());
        recipe.assemble(handler, List.of(), persistedSelection);
        assertEquals(64, handler.getStackInSlot(0).getCount());
        assertTrue(handler.getStackInSlot(0).is(Items.EMERALD));
        assertEquals(1, handler.getStackInSlot(1).getCount());
        assertTrue(handler.getStackInSlot(1).is(Items.EMERALD));
    }

    @Test
    void rollbackFailureIsNonRetriableCorruption() {
        TestRecipe recipe = recipe(new IngredientItem[0], new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 65), 1.0f)}, new FluidStack[0], new FluidStack[0]);
        RollbackFailingOutputHandler handler = new RollbackFailingOutputHandler();
        assertThrows(RecipeTransactionCorruptedException.class, () -> recipe.assemble(handler, List.of(), new int[] {0}));
        assertEquals(64, handler.getStackInSlot(0).getCount());
    }

    private static TestRecipe recipe(IngredientItem[] inputs, OutputItem[] outputs, FluidStack[] fluidInputs, FluidStack[] fluidOutputs) {
        return new TestRecipe(Identifier.fromNamespaceAndPath("nstutlib", "transaction_test"),
                new ModRecipeData(inputs, outputs, fluidInputs, fluidOutputs, 100));
    }

    private static final class TestRecipe extends ModRecipe<TestRecipe> {
        private TestRecipe(Identifier id, ModRecipeData data) { super(id, data, null, null); }
        @Override protected TestRecipe createInstance(Identifier id, ModRecipeData recipeContainer) { return new TestRecipe(id, recipeContainer); }
    }

    private static final class OneShotDivergingOutputHandler extends ItemStackHandler {
        private boolean diverged;
        private OneShotDivergingOutputHandler() { super(2); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && slot == 1 && !diverged) { diverged = true; return stack; }
            return super.insertItem(slot, stack, simulate);
        }
    }

    private static final class DivergingOutputHandler extends ItemStackHandler {
        private DivergingOutputHandler() { super(2); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && slot == 1) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    }

    private static final class RollbackFailingOutputHandler extends ItemStackHandler {
        private boolean mutationStarted;
        private RollbackFailingOutputHandler() { super(2); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && slot == 1) return stack;
            ItemStack remainder = super.insertItem(slot, stack, simulate);
            if (!simulate && slot == 0) mutationStarted = true;
            return remainder;
        }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (mutationStarted) throw new IllegalStateException("synthetic rollback failure");
            super.setStackInSlot(slot, stack);
        }
    }

    private static final class DivergingInputHandler extends ItemStackHandler {
        private DivergingInputHandler() { super(2); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate && slot == 1) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
    }

    private static final class DivergingFluidTank extends FluidTank {
        private DivergingFluidTank(int capacity) { super(capacity); }
        @Override public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            if (action.execute()) return 0;
            return super.fill(resource, action);
        }
    }
}
