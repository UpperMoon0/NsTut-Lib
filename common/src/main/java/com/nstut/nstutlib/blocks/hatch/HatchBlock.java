package com.nstut.nstutlib.blocks.hatch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HatchBlock extends BaseEntityBlock {

    protected HatchBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    // Subclasses will provide the specific BlockEntity
    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState);
}
