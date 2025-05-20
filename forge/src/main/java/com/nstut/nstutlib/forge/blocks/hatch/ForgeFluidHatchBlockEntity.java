package com.nstut.nstutlib.forge.blocks.hatch;

import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ForgeFluidHatchBlockEntity extends FluidHatchBlockEntity {
    private final FluidTank fluidTank = createFluidHandler();
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> fluidTank);

    public ForgeFluidHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ForgeFluidHatchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(NsTutLibBlockEntities.FLUID_HATCH_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    private FluidTank createFluidHandler() {
        return new FluidTank(FluidHatchBlockEntity.FLUID_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    private net.minecraftforge.fluids.FluidStack toForgeStack(FluidStack stack) {
        if (stack.isEmpty()) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), (int) stack.getAmount(), stack.getNbt());
    }

    private FluidStack fromForgeStack(net.minecraftforge.fluids.FluidStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.empty();
        }
        return FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getTag());
    }

    @Override
    public long insert(@NotNull FluidStack fluidStack, boolean simulate) {
        if (fluidStack.isEmpty() || !isFluidValid(0, fluidStack)) {
            return 0;
        }
        int filled = fluidTank.fill(toForgeStack(fluidStack),
                simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        return filled;
    }

    @Override
    public @NotNull FluidStack extract(long amount, boolean simulate) {
        if (amount <= 0) {
            return FluidStack.empty();
        }
        net.minecraftforge.fluids.FluidStack extracted = fluidTank.drain((int) amount, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        return fromForgeStack(extracted);
    }

    @Override
    public int getTankCount() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank == 0) {
            return fromForgeStack(fluidTank.getFluid());
        }
        return FluidStack.empty();
    }

    @Override
    public long getTankCapacity(int tank) {
        if (tank == 0) {
            return fluidTank.getCapacity();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
        if (tank == 0) {
            return fluidTank.isFluidValid(toForgeStack(fluidStack));
        }
        return false;
    }

    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        fluidTank.readFromNBT(pTag.getCompound("fluid"));
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        super.saveAdditional(pTag);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.writeToNBT(fluidTag);
        pTag.put("fluid", fluidTag);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
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
    }
}
