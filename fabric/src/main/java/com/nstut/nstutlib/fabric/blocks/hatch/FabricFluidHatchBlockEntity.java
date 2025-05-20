package com.nstut.nstutlib.fabric.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FabricFluidHatchBlockEntity extends FluidHatchBlockEntity {

    private final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<FluidVariant>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidHatchBlockEntity.FLUID_CAPACITY_MB; // Correctly reference from parent
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
        }
    };

    // Constructor for @ExpectPlatform, will be called by NsTutLibBlockEntitiesImpl
    public FabricFluidHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public FabricFluidHatchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(NsTutLibBlockEntities.FLUID_HATCH_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public long insert(@NotNull FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return 0;
        }
        FluidVariant fluidVariant = FluidVariant.of(stack.getFluid(), stack.getTag());
        long amountInApiUnits = stack.getAmount();

        try (Transaction transaction = Transaction.openOuter()) {
            long insertedAmountApi = fluidStorage.insert(fluidVariant, amountInApiUnits, transaction);
            if (!simulate && insertedAmountApi > 0) {
                transaction.commit();
            }
            return insertedAmountApi;
        }
    }

    @Override
    public @NotNull FluidStack extract(long amount, boolean simulate) {
        if (amount <= 0 || fluidStorage.isResourceBlank()) {
            return FluidStack.empty(); // Use FluidStack.empty()
        }
        FluidVariant resource = fluidStorage.getResource();
        long amountInApiUnits = amount;

        try (Transaction transaction = Transaction.openOuter()) {
            long extractedAmountApi = fluidStorage.extract(resource, amountInApiUnits, transaction);
            if (extractedAmountApi > 0) {
                if (!simulate) {
                    transaction.commit();
                }
                return FluidStack.create(resource.getFluid(), extractedAmountApi, resource.getNbt());
            }
            return FluidStack.empty(); // Use FluidStack.empty()
        }
    }

    @Override
    public int getTankCount() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank == 0 && !fluidStorage.isResourceBlank()) {
            return FluidStack.create(fluidStorage.getResource().getFluid(), fluidStorage.getAmount(), fluidStorage.getResource().getNbt());
        }
        return FluidStack.empty(); // Use FluidStack.empty()
    }

    @Override
    public long getTankCapacity(int tank) {
        if (tank == 0) {
            return FluidHatchBlockEntity.FLUID_CAPACITY_MB;
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank == 0) {
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag fluidTag = new CompoundTag();
        fluidTag.put("variant", fluidStorage.getResource().toNbt());
        fluidTag.putLong("amount", fluidStorage.getAmount());
        tag.put("fluidTank", fluidTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("fluidTank")) {
            CompoundTag fluidTag = tag.getCompound("fluidTank");
            FluidVariant variant = FluidVariant.fromNbt(fluidTag.getCompound("variant"));
            long amount = fluidTag.getLong("amount");
            try (Transaction transaction = Transaction.openOuter()) {
                fluidStorage.extract(fluidStorage.getResource(), fluidStorage.getAmount(), transaction); 
                fluidStorage.insert(variant, amount, transaction);
                transaction.commit();
            }
        }
    }

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }
}
