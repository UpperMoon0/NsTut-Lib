package com.nstut.nstutlib.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
public class OutputItem extends RecipeItem {
    private float chance;

    public OutputItem(ItemStack itemStack, float chance) {
        super(itemStack);
        this.chance = chance;
    }

    public OutputItem(ItemStackTemplate itemStackTemplate, float chance) {
        super(itemStackTemplate);
        this.chance = chance;
    }
}
