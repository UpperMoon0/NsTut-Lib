package com.nstut.nstutlib.items;

import com.mojang.logging.LogUtils;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SmartHammer extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public SmartHammer(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        /*
        if (level.isClientSide && Minecraft.getInstance().hitResult != null
                && Minecraft.getInstance().hitResult.getType() == HitResult.Type.MISS
                && NsTutLib.IS_DEV_ENV) {
            Minecraft.getInstance().setScreen(new SmartHammerScreen(level));
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        */

        if (!level.isClientSide && Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) Minecraft.getInstance().hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (block instanceof MachineBlock && player.isShiftKeyDown()) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof MachineBlockEntity machineBlockEntity) {
                    MultiblockPattern pattern = machineBlockEntity.getMultiblockPattern();
                    buildStructure(level,
                                blockPos,
                                pattern,
                                machineBlockEntity.getSouthOffsetX(),
                                machineBlockEntity.getSouthOffsetY(),
                                machineBlockEntity.getSouthOffsetZ());
                    return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
                }
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void buildStructure(Level level,
                                BlockPos controllerPos,
                                MultiblockPattern pattern,
                                int conSouthOffsetX,
                                int conSouthOffsetY,
                                int conSouthOffsetZ) {

        // Map for alternative items
        Map<Block, Block> alternativeItems = new HashMap<>();
        alternativeItems.put(Blocks.FARMLAND, Blocks.DIRT);

        MultiblockBlock[][][] blockArray = pattern.getPattern();

        BlockState controllerState = level.getBlockState(controllerPos);
        Direction controllerFacing = controllerState.getValue(HorizontalDirectionalBlock.FACING);

        Player player = level.getNearestPlayer(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ(), 10, false);
        if (player == null) return;

        for (int y = blockArray.length - 1; y >= 0; y--) {
            for (int z = 0; z < blockArray[y].length; z++) {
                for (int x = 0; x < blockArray[y][z].length; x++) {
                    MultiblockBlock block = blockArray[y][z][x];
                    if (block != null) {
                        BlockPos targetPos = MultiblockPattern.rotateBlockPos(
                                controllerPos,
                                conSouthOffsetX,
                                conSouthOffsetY,
                                conSouthOffsetZ,
                                blockArray.length,
                                blockArray[y].length,
                                x,
                                y,
                                z,
                                controllerState);

                        BlockState newState = block.getBlock().defaultBlockState();
                        Map<String, String> previousState = block.getStates();

                        if (previousState != null && previousState.containsKey("facing")) {
                            String previousFacing = previousState.get("facing");
                            Direction previousDirection = Direction.byName(previousFacing);
                            if (previousDirection != null) {
                                Direction rotatedFacing = MultiblockPattern.rotateHorizontalDirection(controllerFacing, previousDirection);
                                Map<String, String> mutableState = new HashMap<>(previousState);
                                mutableState.put("facing", rotatedFacing.getName());
                                previousState = mutableState;
                            }
                        }

                        BlockState modifiedState = applyBlockStates(newState, previousState);

                        // Check if it's the controller block's position and if the player is in survival mode
                        if (block.getBlock() == Blocks.WATER || targetPos.equals(controllerPos) || player.isCreative()) {
                            // In survival mode, place the block without checking inventory at the controller block
                            level.setBlock(targetPos, modifiedState, 3);
                        } else {
                            // Check if the block has an alternative item
                            Block blockToPlace = block.getBlock();
                            if (alternativeItems.containsKey(blockToPlace)) {
                                blockToPlace = alternativeItems.get(blockToPlace);
                            }

                            ItemStack requiredStack = new ItemStack(blockToPlace, 1);
                            boolean itemFound = false;

                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack stackInSlot = player.getInventory().getItem(i);
                                if (stackInSlot.is(requiredStack.getItem()) && stackInSlot.getCount() >= requiredStack.getCount()) {
                                    // Set the block in the world
                                    level.setBlock(targetPos, modifiedState, 3);

                                    // Reduce the stack by 1 (placing 1 block)
                                    stackInSlot.shrink(1);

                                    itemFound = true;
                                    break;
                                }
                            }

                            if (!itemFound) {
                                if (!level.isClientSide) {
                                    String errorMessage = "Item not found in inventory: " + requiredStack.getItem().getName(requiredStack).getString();
                                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(errorMessage), true);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockState applyBlockStates(BlockState currentState, Map<String, String> states) {
        if (states == null || states.isEmpty()) return currentState;

        BlockState modifiedState = currentState;
        StateDefinition<Block, BlockState> stateDefinition = currentState.getBlock().getStateDefinition();

        for (Map.Entry<String, String> entry : states.entrySet()) {
            Property<?> property = stateDefinition.getProperty(entry.getKey());
            if (property != null) {
                modifiedState = applyState(modifiedState, property, entry.getValue());
            }
        }

        return modifiedState;
    }

    private <T extends Comparable<T>> BlockState applyState(BlockState state, Property<T> property, String value) {
        T parsedValue = property.getValue(value).orElse(null);
        if (parsedValue != null) {
            return state.setValue(property, parsedValue);
        }
        return state;
    }
}
