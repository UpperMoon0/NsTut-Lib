package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipePreflightTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Items.DIAMOND.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
        Items.EMERALD.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
        Items.GOLD_INGOT.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
    }

    @Test
    void optionalUnselectedOutputDoesNotBlockInputPreflight() {
        TestRecipe recipe = new TestRecipe(
                Identifier.fromNamespaceAndPath("nstutlib", "preflight_test"),
                new ModRecipeData(
                        new IngredientItem[] {new IngredientItem(new ItemStack(Items.DIAMOND), true)},
                        new OutputItem[] {new OutputItem(new ItemStack(Items.EMERALD), 0.5f)},
                        new FluidStack[0],
                        new FluidStack[0],
                        100));

        ItemStackHandler input = new ItemStackHandler(1);
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        ItemStackHandler blockedOutput = new ItemStackHandler(1);
        blockedOutput.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 64));

        assertFalse(recipe.recipeMatch(input, List.of(), blockedOutput, List.of()));
        assertTrue(RecipePreflight.matchesInputs(recipe, input, List.of()));
        assertTrue(recipe.canFitOutputs(blockedOutput, List.of(), new int[0]));
        assertTrue(recipe.tryConsumeIngredients(input, List.of()));
        assertTrue(input.getStackInSlot(0).isEmpty());
    }

    private static final class TestRecipe extends ModRecipe<TestRecipe> {
        private TestRecipe(Identifier id, ModRecipeData data) {
            super(id, data, null, null);
        }

        @Override
        protected TestRecipe createInstance(Identifier id, ModRecipeData recipeContainer) {
            return new TestRecipe(id, recipeContainer);
        }
    }
}
