package com.nstut.nstutlib.fabric.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.EnergyHatchBlockEntity;
import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class FabricEnergyHatchBlockEntity extends EnergyHatchBlockEntity {

    private final SimpleEnergyStorage energyStorage;

    public FabricEnergyHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        // Use TIER_1 as a default, or make it configurable if needed
        this(pType, pPos, pBlockState, EnergyTier.TIER_1);
    }

    // Allow specifying tier if needed by subclasses or specific block instances
    public FabricEnergyHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, EnergyTier tier) {
        super(pType, pPos, pBlockState, tier);
        this.energyStorage = new SimpleEnergyStorage(tier.getCapacity(), tier.getTransferRate(), tier.getTransferRate()) {
            @Override
            protected void onFinalCommit() {
                setChanged();
            }
        };
    }

    // Constructor to match the call in NsTutLibBlockEntitiesImpl.java, ignoring the boolean for now
    public FabricEnergyHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, EnergyTier tier, boolean isInput /* Ignored for now */) {
        this(pType, pPos, pBlockState, tier);
        // The boolean `isInput` could be used here to further configure the energyStorage if needed,
        // e.g., by setting maxInsert or maxExtract to 0 based on its value.
        // For now, it defaults to the tier's transfer rate for both.
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        energyStorage.amount = pTag.getLong("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putLong("energy", energyStorage.amount);
    }

    // Delegate EnergyHatchBlockEntity abstract methods to SimpleEnergyStorage
    @Override
    public long getEnergyStored() {
        return energyStorage.getAmount();
    }

    @Override
    public long getMaxEnergyStored() {
        return energyStorage.getCapacity();
    }

    @Override
    public boolean canExtract() {
        return energyStorage.supportsExtraction();
    }

    @Override
    public boolean canReceive() {
        return energyStorage.supportsInsertion();
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        // TechReborn's SimpleEnergyStorage insert method doesn't directly use a simulate flag in its signature.
        // It relies on transactions for simulation.
        // If simulate is true, we open a transaction. If false, we pass null (or an active transaction if available).
        try (var transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            long inserted = energyStorage.insert(maxReceive, transaction);
            if (simulate) {
                transaction.abort(); // Abort if only simulating
            } else {
                transaction.commit(); // Commit if not simulating
            }
            return inserted;
        }
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        // Similar to receiveEnergy, handle simulation with transactions.
        try (var transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            long extracted = energyStorage.extract(maxExtract, transaction);
            if (simulate) {
                transaction.abort(); // Abort if only simulating
            } else {
                transaction.commit(); // Commit if not simulating
            }
            return extracted;
        }
    }

    // Method to provide EnergyStorage for Fabric API (e.g., for capabilities)
    public EnergyStorage getFabricEnergyStorage() {
        return energyStorage;
    }
}
