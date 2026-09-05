package com.nstut.nstutlib.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
public class IngredientItem extends RecipeItem {
    private boolean isConsumable;

    public IngredientItem(ItemStack itemStack, boolean isConsumable) {
        super(itemStack);
        this.isConsumable = isConsumable;
    }

    public IngredientItem(ItemStackTemplate itemStackTemplate, boolean isConsumable) {
        super(itemStackTemplate);
        this.isConsumable = isConsumable;
    }
}
