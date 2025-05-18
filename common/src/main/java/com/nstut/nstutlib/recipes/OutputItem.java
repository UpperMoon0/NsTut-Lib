package com.nstut.nstutlib.recipes;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;

@EqualsAndHashCode
@Data
public class OutputItem {
    protected ItemStack itemStack;
    private float chance;

    public OutputItem(ItemStack itemStack, float chance) {
        this.itemStack = itemStack;
        this.chance = chance;
    }
}
