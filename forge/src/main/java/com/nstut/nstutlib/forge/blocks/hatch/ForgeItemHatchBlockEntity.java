package com.nstut.nstutlib.forge.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ForgeItemHatchBlockEntity extends ItemHatchBlockEntity {
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public ForgeItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ForgeItemHatchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(NsTutLibBlockEntities.ITEM_HATCH_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(ITEM_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return ForgeItemHatchBlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return ForgeItemHatchBlockEntity.this.getSlotLimit(slot);
            }
        };
    }

    @Override
    public @NotNull ItemStack insert(@NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            stack = itemHandler.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack extract(int amount, boolean simulate) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack extracted = itemHandler.extractItem(i, amount, simulate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extract(int slot, int amount, boolean simulate) {
        return itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotCount() {
        return itemHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64; // Default Minecraft slot limit
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true; // Allow any item by default
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getSlotCount(), ItemStack.EMPTY);
        for (int i = 0; i < getSlotCount(); i++) {
            items.set(i, getStackInSlot(i));
        }
        return items;
    }

    // Ensure @Nonnull matches the super method from BlockEntity
    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inv"));
    }

    // Ensure @Nonnull matches the super method from BlockEntity
    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("inv", itemHandler.serializeNBT());
    }

    // Use @NotNull for the return type and cap parameter as per ICapabilityProvider
    // and ensure it's compatible with how CapabilityProvider<BlockEntity> defines it.
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        // No need to re-create handler here as it's tied to itemHandler which persists
    }
}
