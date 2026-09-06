package com.nstut.nstutlib.recipes;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

@GameTestHolder(NsTutLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ModRecipeGameTests {
    private ModRecipeGameTests() {}

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 100)
    public static void transactionRollbackAndRetry(GameTestHelper helper) {
        TestRecipe recipe = recipe(new IngredientItem[0],
                new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 65), 1.0f)},
                new FluidStack[0], new FluidStack[0]);

        DivergingOutputHandler alwaysDiverges = new DivergingOutputHandler();
        expectThrows(RecipeTransactionException.class, () -> recipe.assemble(alwaysDiverges, List.of()),
                "Partial item-output commit must fail transaction");
        helper.assertTrue(alwaysDiverges.getStackInSlot(0).isEmpty() && alwaysDiverges.getStackInSlot(1).isEmpty(),
                "Partial item-output commit must roll back all slots");

        OneShotDivergingOutputHandler retryable = new OneShotDivergingOutputHandler();
        expectThrows(RecipeTransactionException.class, () -> recipe.assemble(retryable, List.of()),
                "First divergent output commit must be recoverable");
        helper.assertTrue(retryable.getStackInSlot(0).isEmpty() && retryable.getStackInSlot(1).isEmpty(),
                "Recoverable divergence must restore output inventory before retry");
        recipe.assemble(retryable, List.of());
        helper.assertTrue(retryable.getStackInSlot(0).getCount() == 64 && retryable.getStackInSlot(1).getCount() == 1,
                "Retry must commit the full output exactly once");
        helper.succeed();
    }

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 100)
    public static void fluidAndIngredientRollback(GameTestHelper helper) {
        TestRecipe fluidRecipe = recipe(new IngredientItem[0], new OutputItem[0], new FluidStack[0],
                new FluidStack[] {new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1500)});
        FluidTank first = new FluidTank(1000);
        FluidTank second = new DivergingFluidTank(1000);
        expectThrows(RecipeTransactionException.class, () -> fluidRecipe.assemble(null, List.of(first, second)),
                "Partial fluid-output commit must fail transaction");
        helper.assertTrue(first.isEmpty() && second.isEmpty(), "Partial fluid-output commit must roll back every tank");

        TestRecipe ingredientRecipe = recipe(
                new IngredientItem[] {
                        new IngredientItem(new ItemStack(Items.DIAMOND), true),
                        new IngredientItem(new ItemStack(Items.GOLD_INGOT), true)
                }, new OutputItem[0], new FluidStack[0], new FluidStack[0]);
        DivergingInputHandler inputs = new DivergingInputHandler();
        inputs.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        inputs.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT));
        expectThrows(RecipeTransactionException.class, () -> ingredientRecipe.tryConsumeIngredients(inputs, List.of()),
                "Partial ingredient consumption must fail transaction");
        helper.assertTrue(inputs.getStackInSlot(0).getCount() == 1 && inputs.getStackInSlot(1).getCount() == 1,
                "Partial ingredient consumption must restore all inputs");
        helper.succeed();
    }

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 100)
    public static void persistedChanceAndRollbackCorruption(GameTestHelper helper) {
        TestRecipe chanceRecipe = recipe(new IngredientItem[0],
                new OutputItem[] {
                        new OutputItem(new ItemStack(Items.DIAMOND, 65), 0.5f),
                        new OutputItem(new ItemStack(Items.EMERALD, 65), 0.5f)
                }, new FluidStack[0], new FluidStack[0]);
        OneShotDivergingOutputHandler retryable = new OneShotDivergingOutputHandler();
        int[] persistedSelection = {1};
        expectThrows(RecipeTransactionException.class,
                () -> chanceRecipe.assemble(retryable, List.of(), persistedSelection),
                "Divergent commit must not replace persisted chance selection");
        chanceRecipe.assemble(retryable, List.of(), persistedSelection);
        helper.assertTrue(retryable.getStackInSlot(0).is(Items.EMERALD)
                        && retryable.getStackInSlot(0).getCount() == 64
                        && retryable.getStackInSlot(1).is(Items.EMERALD)
                        && retryable.getStackInSlot(1).getCount() == 1,
                "Retry must reuse the persisted probabilistic output selection");

        TestRecipe corruptionRecipe = recipe(new IngredientItem[0],
                new OutputItem[] {new OutputItem(new ItemStack(Items.DIAMOND, 65), 1.0f)},
                new FluidStack[0], new FluidStack[0]);
        RollbackFailingOutputHandler corrupting = new RollbackFailingOutputHandler();
        expectThrows(RecipeTransactionCorruptedException.class,
                () -> corruptionRecipe.assemble(corrupting, List.of(), new int[] {0}),
                "Rollback restoration failure must be non-retriable corruption");
        helper.assertTrue(corrupting.getStackInSlot(0).getCount() == 64,
                "Synthetic rollback failure must leave evidence of the partial mutation");
        helper.succeed();
    }

    private static void expectThrows(Class<? extends Throwable> expected, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (expected.isInstance(thrown)) return;
            throw new AssertionError(message + ": expected " + expected.getSimpleName() + " but got " + thrown, thrown);
        }
        throw new AssertionError(message + ": expected " + expected.getSimpleName());
    }

    private static TestRecipe recipe(IngredientItem[] inputs, OutputItem[] outputs,
                                     FluidStack[] fluidInputs, FluidStack[] fluidOutputs) {
        return new TestRecipe(new ResourceLocation(NsTutLib.MOD_ID, "gametest_transaction"),
                new ModRecipeData(inputs, outputs, fluidInputs, fluidOutputs, 100));
    }

    private static final class TestRecipe extends ModRecipe<TestRecipe> {
        private TestRecipe(ResourceLocation id, ModRecipeData data) { super(id, data, null, null); }
        @Override protected TestRecipe createInstance(ResourceLocation id, ModRecipeData recipeContainer) {
            return new TestRecipe(id, recipeContainer);
        }
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
