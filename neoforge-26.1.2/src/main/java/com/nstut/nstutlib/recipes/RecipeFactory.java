package com.nstut.nstutlib.recipes;

import net.minecraft.resources.Identifier;

public interface RecipeFactory<T extends ModRecipe<?>> {
    T create(Identifier id, ModRecipeData recipeData);
}
