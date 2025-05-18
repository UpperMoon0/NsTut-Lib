package com.nstut.nstutlib.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;

@EqualsAndHashCode
@Data
public class IngredientItem {
    protected ItemStack itemStack;
    private boolean isConsumable;

    public IngredientItem(ItemStack itemStack, boolean isConsumable) {
        this.itemStack = itemStack;
        this.isConsumable = isConsumable;
    }
}
