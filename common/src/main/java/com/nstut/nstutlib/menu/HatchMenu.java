package com.nstut.nstutlib.menu;

import com.nstut.nstutlib.blocks.hatch.HatchBlockEntity;
// import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity; // Will be used in ItemHatchMenu
import com.nstut.nstutlib.core.registry.NsTutLibMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class HatchMenu extends AbstractContainerMenu {
    protected final HatchBlockEntity hatchBlockEntity;
    protected static final int PLAYER_INVENTORY_ROWS = 3;
    protected static final int PLAYER_INVENTORY_COLUMNS = 9;
    protected static final int PLAYER_HOTBAR_SLOTS = 9;
    protected static final int PLAYER_INVENTORY_X_OFFSET = 8;
    protected static final int PLAYER_INVENTORY_Y_OFFSET = 84;
    // HATCH_INVENTORY_X_OFFSET and Y_OFFSET will be defined in subclasses or passed to addHatchSlots

    // Constructor for server-side
    protected HatchMenu(MenuType<?> menuType, int windowId, Inventory playerInventory, HatchBlockEntity hatchBlockEntity) {
        super(menuType, windowId);
        this.hatchBlockEntity = hatchBlockEntity;

        addHatchSlots(playerInventory); // Call abstract method
        addPlayerInventorySlots(playerInventory);
    }

    // Constructor for client-side (called via MenuRegistry.of)
    // This constructor will be called by subclasses
    protected HatchMenu(MenuType<?> menuType, int windowId, Inventory playerInventory, FriendlyByteBuf friendlyByteBuf) {
        this(menuType, windowId, playerInventory, getBlockEntity(playerInventory, friendlyByteBuf));
    }

    protected abstract void addHatchSlots(Inventory playerInventory);
    protected abstract int getHatchSlotCount();


    private void addPlayerInventorySlots(Inventory playerInventory) {
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

            int hatchSlotCount = getHatchSlotCount();
            int playerInventoryStartIndex = hatchSlotCount;
            int playerHotbarStartIndex = playerInventoryStartIndex + PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;
            int totalSlots = this.slots.size();

            if (pIndex < hatchSlotCount) { // Moving from hatch to player inventory
                if (!this.moveItemStackTo(slotStack, playerInventoryStartIndex, totalSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Moving from player inventory to hatch or within player inventory
                if (hatchSlotCount > 0 && pIndex >= playerInventoryStartIndex) { // Attempt to move to hatch slots
                    if (!this.moveItemStackTo(slotStack, 0, hatchSlotCount, false)) {
                        // If failed to move to hatch, try moving within player inventory
                        if (pIndex < playerHotbarStartIndex) { // From main player inventory to hotbar
                            if (!this.moveItemStackTo(slotStack, playerHotbarStartIndex, totalSlots, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else { // From hotbar to main player inventory
                            if (!this.moveItemStackTo(slotStack, playerInventoryStartIndex, playerHotbarStartIndex, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                } else { // Moving within player inventory (no hatch slots or not targeting them)
                    if (pIndex < playerHotbarStartIndex) { // From main player inventory to hotbar
                        if (!this.moveItemStackTo(slotStack, playerHotbarStartIndex, totalSlots, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else { // From hotbar to main player inventory
                        if (!this.moveItemStackTo(slotStack, playerInventoryStartIndex, playerHotbarStartIndex, false)) {
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
