package com.nstut.nstutlib.blocks.hatch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer; // Added import
import net.minecraft.world.InteractionHand; // Added import
import net.minecraft.world.InteractionResult; // Added import
import net.minecraft.world.MenuProvider; // Added import
import net.minecraft.world.entity.player.Player; // Added import
import net.minecraft.world.level.Level; // Added import
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult; // Added import
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

    // Added for GUI interaction
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof MenuProvider menuProvider) {
                pPlayer.openMenu(menuProvider);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
