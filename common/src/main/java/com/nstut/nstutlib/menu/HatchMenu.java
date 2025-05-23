package com.nstut.nstutlib.menu;

import com.nstut.nstutlib.blocks.hatch.HatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HatchMenu extends AbstractContainerMenu {
    private final HatchBlockEntity hatchBlockEntity;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_HOTBAR_SLOTS = 9;
    private static final int PLAYER_INVENTORY_X_OFFSET = 8;
    private static final int PLAYER_INVENTORY_Y_OFFSET = 84; 
    private static final int HATCH_INVENTORY_X_OFFSET = 8;
    private static final int HATCH_INVENTORY_Y_OFFSET = 18; 

    // Constructor for server-side
    public HatchMenu(int windowId, Inventory playerInventory, HatchBlockEntity hatchBlockEntity) {
        super(NsTutLibMenuTypes.HATCH_MENU.get(), windowId);
        this.hatchBlockEntity = hatchBlockEntity;

        // Add hatch inventory slots if it's an ItemHatchBlockEntity
        if (hatchBlockEntity instanceof ItemHatchBlockEntity itemHatch) {
            int slotCount = itemHatch.getContainerSize(); // Assuming getContainerSize() exists or can be added
            for (int i = 0; i < slotCount; ++i) {
                // Adjust x and y position as needed for your GUI layout
                // For a 3x3 grid like ItemHatchBlockEntity.ITEM_SLOT_COUNT = 9
                int x = HATCH_INVENTORY_X_OFFSET + (i % 3) * 18;
                int y = HATCH_INVENTORY_Y_OFFSET + (i / 3) * 18;
                this.addSlot(new Slot(itemHatch, i, x, y));
            }
        }

        // Player inventory
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; ++row) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * PLAYER_INVENTORY_COLUMNS + PLAYER_HOTBAR_SLOTS,
                        PLAYER_INVENTORY_X_OFFSET + col * 18, PLAYER_INVENTORY_Y_OFFSET + row * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < PLAYER_HOTBAR_SLOTS; ++i) {
            this.addSlot(new Slot(playerInventory, i, PLAYER_INVENTORY_X_OFFSET + i * 18, PLAYER_INVENTORY_Y_OFFSET + 58));
        }
    }

    // Constructor for client-side (called via MenuRegistry.of)
    public HatchMenu(int windowId, Inventory playerInventory, FriendlyByteBuf friendlyByteBuf) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, friendlyByteBuf));
    }

    private static HatchBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf friendlyByteBuf) {
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos());
        if (blockEntity instanceof HatchBlockEntity) {
            return (HatchBlockEntity) blockEntity;
        }
        throw new IllegalStateException("Block entity is not of type HatchBlockEntity! " + blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // Number of slots in the hatch inventory
            int hatchSlotCount = (hatchBlockEntity instanceof ItemHatchBlockEntity itemHatch) ? itemHatch.getContainerSize() : 0;

            if (pIndex < hatchSlotCount) { // Moving from hatch to player inventory
                if (!this.moveItemStackTo(slotStack, hatchSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Moving from player inventory to hatch
                if (hatchBlockEntity instanceof ItemHatchBlockEntity) { // Only if it's an item hatch
                    if (!this.moveItemStackTo(slotStack, 0, hatchSlotCount, false)) {
                        return ItemStack.EMPTY;
                    }
                } else { // If not an item hatch, or trying to move to a non-item hatch slot
                    // If moving from player inventory to player inventory (e.g. main to hotbar)
                    if (pIndex < this.slots.size() - PLAYER_HOTBAR_SLOTS) { // from main player inv
                         if (!this.moveItemStackTo(slotStack, this.slots.size() - PLAYER_HOTBAR_SLOTS, this.slots.size(), false)) { // to hotbar
                            return ItemStack.EMPTY;
                        }
                    } else { // from hotbar
                        if (!this.moveItemStackTo(slotStack, hatchSlotCount, this.slots.size() - PLAYER_HOTBAR_SLOTS, false)) { // to main player inv
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.hatchBlockEntity.stillValid(pPlayer); // Assuming stillValid method in HatchBlockEntity
    }

    public HatchBlockEntity getHatchBlockEntity() {
        return hatchBlockEntity;
    }
}
