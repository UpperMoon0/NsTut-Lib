package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.core.NsTutLibIdentifiers;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class InputItemHatchBlock extends HatchBlock {
    public InputItemHatchBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return NsTutLibBlockEntities.INPUT_ITEM_HATCH_BLOCK_ENTITY.get().create(pPos, pState);
    }
}
