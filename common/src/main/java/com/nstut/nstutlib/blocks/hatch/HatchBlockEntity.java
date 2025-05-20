package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class HatchBlockEntity extends BlockEntity {

    @Nullable
    protected MachineBlockEntity controller;
    // TODO: Consider how the controller is linked. Maybe via NBT or searching on load?

    public HatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void setController(@Nullable MachineBlockEntity controller) {
        this.controller = controller;
        // TODO: Mark dirty and save controller's position if needed for persistence
        setChanged();
    }

    @Nullable
    public MachineBlockEntity getController() {
        // TODO: Add logic to find controller if null and part of a multiblock
        return controller;
    }
}
