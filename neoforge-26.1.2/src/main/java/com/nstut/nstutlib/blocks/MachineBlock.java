package com.nstut.nstutlib.blocks;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.BiFunction;

public class MachineBlock extends BaseEntityBlock {
    public static final BooleanProperty OPERATING = BooleanProperty.create("operating");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final Logger LOGGER = LogUtils.getLogger();
    private final BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> blockEntityFactory;

    public MachineBlock(BlockBehaviour.Properties properties,
                        BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> blockEntityFactory) {
        super(properties);
        this.blockEntityFactory = Objects.requireNonNull(blockEntityFactory, "blockEntityFactory");
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPERATING, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        try {
            return blockEntityFactory.apply(pos, state);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to create machine block entity at {}", pos, exception);
            return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPERATING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
                                                        @NotNull Level level,
                                                        @NotNull BlockPos pos,
                                                        @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (player instanceof ServerPlayer serverPlayer && blockEntity instanceof MenuProvider menuProvider) {
            serverPlayer.openMenu(menuProvider, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level level,
                                                                  @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<U> blockEntityType) {
        if (level.isClientSide()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MachineBlockEntity machineBlockEntity) {
                MachineBlockEntity.serverTick(tickLevel, pos, tickState, machineBlockEntity);
            }
        };
    }
}
