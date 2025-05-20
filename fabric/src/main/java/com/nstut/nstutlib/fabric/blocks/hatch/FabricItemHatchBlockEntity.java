package com.nstut.nstutlib.fabric.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.core.registry.NsTutLibBlocks;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class FabricItemHatchBlockEntity extends ItemHatchBlockEntity {

    private final SimpleContainer inventory = new SimpleContainer(ITEM_SLOT_COUNT);
    // Fabric's InventoryStorage can wrap a SimpleContainer to provide Storage<ItemVariant>
    // This provides a view over the whole inventory.
    private final Storage<ItemVariant> itemStorage = InventoryStorage.of(inventory, null);
    // For slot-specific operations, we can create views for each slot.
    private final List<SingleSlotStorage<ItemVariant>> slotStorages = NonNullList.withSize(ITEM_SLOT_COUNT, ItemStack.EMPTY)
            .stream()
            .map(s -> InventoryStorage.of(inventory, null).getSlot(inventory.getContainerSize() -1)) // Placeholder, will be properly initialized
            .collect(Collectors.toList());


    public FabricItemHatchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        // The BlockEntityType is now passed from the common registration
        super( NsTutLibBlockEntities.ITEM_HATCH_BLOCK_ENTITY.get(), // Corrected BE type field name
                pPos, pBlockState);
        // Initialize slotStorages correctly
        for (int i = 0; i < ITEM_SLOT_COUNT; i++) {
            final int slot = i;
            // Create a SingleSlotStorage view for each slot in the inventory
            slotStorages.set(i, new SingleSlotStorage<ItemVariant>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return InventoryStorage.of(inventory, null).getSlot(slot).insert(resource, maxAmount, transaction);
                }

                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return InventoryStorage.of(inventory, null).getSlot(slot).extract(resource, maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).isResourceBlank();
                }

                @Override
                public ItemVariant getResource() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getResource();
                }

                @Override
                public long getAmount() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getAmount();
                }

                @Override
                public long getCapacity() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getCapacity();
                }
            });
        }
    }

    // Constructor for @ExpectPlatform, will be called by NsTutLibBlockEntitiesImpl
    public FabricItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
         // Initialize slotStorages correctly
        for (int i = 0; i < ITEM_SLOT_COUNT; i++) {
            final int slot = i;
            // Create a SingleSlotStorage view for each slot in the inventory
            slotStorages.set(i, new SingleSlotStorage<ItemVariant>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return InventoryStorage.of(inventory, null).getSlot(slot).insert(resource, maxAmount, transaction);
                }

                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return InventoryStorage.of(inventory, null).getSlot(slot).extract(resource, maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).isResourceBlank();
                }

                @Override
                public ItemVariant getResource() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getResource();
                }

                @Override
                public long getAmount() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getAmount();
                }

                @Override
                public long getCapacity() {
                    return InventoryStorage.of(inventory, null).getSlot(slot).getCapacity();
                }
            });
        }
    }


    @Override
    public @NotNull ItemStack insert(@NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // Try inserting into any slot
        try (Transaction transaction = Transaction.openOuter()) {
            long insertedCount = itemStorage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
                setChanged();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink((int) insertedCount);
            return remainder;
        }
    }

    @Override
    public @NotNull ItemStack extract(int amount, boolean simulate) {
        // Try extracting from any slot that has items.
        // This is a simplified approach; a more robust solution might prioritize certain slots
        // or allow specifying which item type to extract.
        for (int i = 0; i < ITEM_SLOT_COUNT; i++) {
            if (!inventory.getItem(i).isEmpty()) {
                return extract(i, amount, simulate);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extract(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= ITEM_SLOT_COUNT || amount <= 0) {
            return ItemStack.EMPTY;
        }
        SingleSlotStorage<ItemVariant> slotView = slotStorages.get(slot);
        if (slotView.isResourceBlank()) {
            return ItemStack.EMPTY;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = slotView.getResource();
            long extractedCount = slotView.extract(resource, amount, transaction);

            if (extractedCount > 0) {
                if (!simulate) {
                    transaction.commit();
                    setChanged();
                }
                return resource.toStack((int) extractedCount);
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotCount() {
        return ITEM_SLOT_COUNT;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= ITEM_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return inventory.getItem(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= ITEM_SLOT_COUNT) {
            return 0;
        }
        return inventory.getMaxStackSize(); // Or a custom limit if needed
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= ITEM_SLOT_COUNT) {
            return false;
        }
        // Allow any item by default, can be overridden for specific hatch behavior
        return true;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot >= 0 && slot < ITEM_SLOT_COUNT) {
            inventory.setItem(slot, stack);
            setChanged();
        }
    }
    
    @Override
    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(ITEM_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < ITEM_SLOT_COUNT; i++) {
            items.set(i, inventory.getItem(i));
        }
        return items;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, getItems());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        NonNullList<ItemStack> items = NonNullList.withSize(ITEM_SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        for(int i = 0; i < items.size(); i++) {
            inventory.setItem(i, items.get(i));
        }
    }
    
    // Provide access to the Fabric-specific storage for other Fabric components if needed
    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    public SingleSlotStorage<ItemVariant> getSlotStorage(int slot) {
        if (slot >= 0 && slot < ITEM_SLOT_COUNT) {
            return slotStorages.get(slot);
        }
        return null;
    }
}
