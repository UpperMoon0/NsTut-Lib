package com.nstut.nstutlib.recipes;

import net.minecraft.resources.ResourceLocation;

public interface RecipeFactory<T extends ModRecipe<?>> {
    T create(ResourceLocation id, ModRecipeData recipeData);
}
