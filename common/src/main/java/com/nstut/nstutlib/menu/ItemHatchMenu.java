package com.nstut.nstutlib.menu;

import com.nstut.nstutlib.blocks.hatch.HatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ItemHatchMenu extends HatchMenu {
    private static final int HATCH_INVENTORY_X_OFFSET = 61; // Example, adjust as needed
    private static final int HATCH_INVENTORY_Y_OFFSET = 16; // Example, adjust as needed

    // Server-side constructor
    public ItemHatchMenu(int windowId, Inventory playerInventory, HatchBlockEntity hatchBlockEntity) {
        super(NsTutLibMenuTypes.ITEM_HATCH_MENU.get(), windowId, playerInventory, hatchBlockEntity);
    }

    // Client-side constructor
    public ItemHatchMenu(int windowId, Inventory playerInventory, FriendlyByteBuf friendlyByteBuf) {
        super(NsTutLibMenuTypes.ITEM_HATCH_MENU.get(), windowId, playerInventory, friendlyByteBuf);
    }

    @Override
    protected void addHatchSlots(Inventory playerInventory) {
        if (hatchBlockEntity instanceof ItemHatchBlockEntity itemHatch) {
            int slotCount = itemHatch.getContainerSize();
            // Assuming a 3x3 grid for 9 slots, adjust layout as needed
            for (int i = 0; i < slotCount; ++i) {
                int x = HATCH_INVENTORY_X_OFFSET + (i % 3) * 18;
                int y = HATCH_INVENTORY_Y_OFFSET + (i / 3) * 18;
                this.addSlot(new Slot(itemHatch, i, x, y));
            }
        }
    }

    @Override
    protected int getHatchSlotCount() {
        if (hatchBlockEntity instanceof ItemHatchBlockEntity itemHatch) {
            return itemHatch.getContainerSize();
        }
        return 0;
    }
}
