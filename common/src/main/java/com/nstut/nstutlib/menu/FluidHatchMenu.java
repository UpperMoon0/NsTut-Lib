package com.nstut.nstutlib.menu;

import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.HatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
// Assuming you have a way to handle fluid container items, e.g., through capabilities or specific item checks.
// You might need a custom Slot class (e.g., FluidContainerSlot) to validate items.

public class FluidHatchMenu extends HatchMenu {
    private static final int FLUID_INPUT_SLOT_X = 62; 
    private static final int FLUID_INPUT_SLOT_Y = 26; 
    private static final int FLUID_OUTPUT_SLOT_X = 62; 
    private static final int FLUID_OUTPUT_SLOT_Y = 52;

    public static final int FLUID_INPUT_SLOT_INDEX = 0;
    public static final int FLUID_OUTPUT_SLOT_INDEX = 1;
    private static final int FLUID_HATCH_SLOT_COUNT = 2;

    // Server-side constructor
    public FluidHatchMenu(int windowId, Inventory playerInventory, HatchBlockEntity hatchBlockEntity) {
        super(NsTutLibMenuTypes.FLUID_HATCH_MENU.get(), windowId, playerInventory, hatchBlockEntity);
    }

    // Client-side constructor
    public FluidHatchMenu(int windowId, Inventory playerInventory, FriendlyByteBuf friendlyByteBuf) {
        super(NsTutLibMenuTypes.FLUID_HATCH_MENU.get(), windowId, playerInventory, friendlyByteBuf);
    }

    @Override
    protected void addHatchSlots(Inventory playerInventory) {
        if (hatchBlockEntity instanceof FluidHatchBlockEntity fluidHatch) {
            // Slot for input fluid container (e.g., bucket, fluid cell)
            this.addSlot(new Slot(fluidHatch, FLUID_INPUT_SLOT_INDEX, FLUID_INPUT_SLOT_X, FLUID_INPUT_SLOT_Y));
            // Slot for output item (e.g., empty bucket, filled bucket)
            this.addSlot(new Slot(fluidHatch, FLUID_OUTPUT_SLOT_INDEX, FLUID_OUTPUT_SLOT_X, FLUID_OUTPUT_SLOT_Y));
            // You might want custom Slot implementations here to restrict what can be placed in these slots.
            // For example, a slot that only accepts items with fluid handler capabilities, or specific bucket types.
        }
    }

    @Override
    protected int getHatchSlotCount() {
        return FLUID_HATCH_SLOT_COUNT; // Two slots: one for input container, one for output container
    }
}
