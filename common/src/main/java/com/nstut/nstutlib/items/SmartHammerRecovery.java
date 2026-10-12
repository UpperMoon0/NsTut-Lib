package com.nstut.nstutlib.items;

import net.minecraft.world.item.ItemStack;

/** Read-only recovery decision. Returned items are copied, never shared with live storage. */
public record SmartHammerRecovery(ItemStack item, String blocker) {
    public SmartHammerRecovery {
        item = item == null ? ItemStack.EMPTY : item.copy();
        if (blocker == null && !item.isEmpty() && item.getCount() != 1)
            throw new IllegalArgumentException("Recover exactly one block item");
    }
    @Override public ItemStack item() { return item.copy(); }
    public static SmartHammerRecovery ready(ItemStack item) { return new SmartHammerRecovery(item, null); }
    public static SmartHammerRecovery blocked(String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Explain the blocker");
        return new SmartHammerRecovery(ItemStack.EMPTY, reason);
    }
    public boolean allowed() { return blocker == null; }
}
