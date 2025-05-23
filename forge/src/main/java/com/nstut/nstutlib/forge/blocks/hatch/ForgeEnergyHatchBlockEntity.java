package com.nstut.nstutlib.forge.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.EnergyHatchBlockEntity;
import com.nstut.nstutlib.util.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import javax.annotation.Nonnull;

public class ForgeEnergyHatchBlockEntity extends EnergyHatchBlockEntity {
    private final EnergyStorage energyStorage;
    private final LazyOptional<EnergyStorage> energyStorageLazyOptional;
    private final boolean isInput;

    public ForgeEnergyHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, EnergyTier tier, boolean isInput) {
        super(type, pos, state, tier);
        this.isInput = isInput;
        this.energyStorage = new EnergyStorage(tier.getCapacity(), tier.getTransferRate(), tier.getTransferRate()) {
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!ForgeEnergyHatchBlockEntity.this.canExtract()) {
                    return 0;
                }
                return super.extractEnergy(maxExtract, simulate);
            }

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (!ForgeEnergyHatchBlockEntity.this.canReceive()) {
                    return 0;
                }
                return super.receiveEnergy(maxReceive, simulate);
            }

            @Override
            public boolean canExtract() {
                return ForgeEnergyHatchBlockEntity.this.isInput ? false : super.canExtract();
            }

            @Override
            public boolean canReceive() {
                return ForgeEnergyHatchBlockEntity.this.isInput ? super.canReceive() : false;
            }
        };
        this.energyStorageLazyOptional = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public long getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public long getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return !isInput && energyStorage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return isInput && energyStorage.canReceive();
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy((int) Math.min(Integer.MAX_VALUE, maxReceive), simulate);
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        return energyStorage.extractEnergy((int) Math.min(Integer.MAX_VALUE, maxExtract), simulate);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyStorageLazyOptional.invalidate();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Energy", energyStorage.serializeNBT());
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        energyStorage.deserializeNBT(tag.get("Energy"));
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(worldPosition);
    }
}
