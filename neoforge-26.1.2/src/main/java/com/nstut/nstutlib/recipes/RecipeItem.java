package com.nstut.nstutlib.recipes;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

@NoArgsConstructor
@Data
public abstract class RecipeItem {
    protected ItemStack itemStack;
    protected ItemStackTemplate itemStackTemplate;

    public RecipeItem(ItemStack itemStack) {
        setItemStack(itemStack);
    }

    public RecipeItem(ItemStackTemplate itemStackTemplate) {
        setItemStackTemplate(itemStackTemplate);
    }

    /**
     * Materializes the runtime stack lazily. 26.1 recipe loading happens before
     * item data components are bound, so codecs must retain ItemStackTemplate
     * rather than constructing ItemStack eagerly.
     */
    public ItemStack getItemStack() {
        if (itemStack == null && itemStackTemplate != null) {
            itemStack = itemStackTemplate.create();
        }
        return itemStack == null ? ItemStack.EMPTY : itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemStackTemplate = itemStack == null || itemStack.isEmpty()
                ? null
                : ItemStackTemplate.fromNonEmptyStack(itemStack);
    }

    public ItemStackTemplate getItemStackTemplate() {
        if (itemStackTemplate == null && itemStack != null && !itemStack.isEmpty()) {
            itemStackTemplate = ItemStackTemplate.fromNonEmptyStack(itemStack);
        }
        return itemStackTemplate;
    }

    public void setItemStackTemplate(ItemStackTemplate itemStackTemplate) {
        this.itemStackTemplate = itemStackTemplate;
        this.itemStack = null;
    }
}
