package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
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

class ModRecipeItemMatcherTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void defaultMatcherRemainsExactWhileSubclassCanSpecializeIngredients() {
        ItemStack required = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack damaged = required.copy();
        damaged.setDamageValue(1);

        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, damaged.copy());

        TestRecipe exact = new TestRecipe(id("exact"), data(required));
        assertFalse(exact.recipeMatch(handler, List.of(), null, List.of()));

        LenientTestRecipe lenient = new LenientTestRecipe(id("lenient"), data(required));
        assertTrue(lenient.recipeMatch(handler, List.of(), null, List.of()));
        assertTrue(lenient.tryConsumeIngredients(handler, List.of()));
        assertTrue(handler.getStackInSlot(0).isEmpty());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("nstutlib", path);
    }

    private static ModRecipeData data(ItemStack required) {
        return new ModRecipeData(
                new IngredientItem[] {new IngredientItem(required, true)},
                new OutputItem[0],
                new FluidStack[0],
                new FluidStack[0],
                0);
    }

    private static class TestRecipe extends ModRecipe<TestRecipe> {
        private TestRecipe(ResourceLocation id, ModRecipeData data) { super(id, data, null, null); }
        @Override protected TestRecipe createInstance(ResourceLocation id, ModRecipeData data) { return new TestRecipe(id, data); }
    }

    private static final class LenientTestRecipe extends ModRecipe<LenientTestRecipe> {
        private LenientTestRecipe(ResourceLocation id, ModRecipeData data) { super(id, data, null, null); }
        @Override protected LenientTestRecipe createInstance(ResourceLocation id, ModRecipeData data) { return new LenientTestRecipe(id, data); }
        @Override protected boolean itemIngredientsMatch(ItemStack required, ItemStack present) {
            return !required.isEmpty() && !present.isEmpty() && required.is(present.getItem());
        }
    }
}
