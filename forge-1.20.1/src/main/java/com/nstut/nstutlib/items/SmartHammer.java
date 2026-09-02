package com.nstut.nstutlib.items;

import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartHammer extends Item {
    private static final int MAX_BUILD_BLOCKS = 32_768;

    public SmartHammer(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack hammer = player.getItemInHand(hand);
        if (level.isClientSide || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(hammer);
        }

        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(hammer);
        }

        BlockPos controllerPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockState controllerState = level.getBlockState(controllerPos);
        if (!(controllerState.getBlock() instanceof MachineBlock)) {
            return InteractionResultHolder.pass(hammer);
        }

        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        if (!(blockEntity instanceof MachineBlockEntity machineBlockEntity)) {
            return InteractionResultHolder.pass(hammer);
        }

        boolean built = buildStructure(
                level,
                player,
                controllerPos,
                machineBlockEntity.getMultiblockPattern(),
                machineBlockEntity.getSouthOffsetX(),
                machineBlockEntity.getSouthOffsetY(),
                machineBlockEntity.getSouthOffsetZ());
        if (built) {
            machineBlockEntity.requestStructureValidation();
            return InteractionResultHolder.consume(hammer);
        }
        return InteractionResultHolder.fail(hammer);
    }

    private boolean buildStructure(Level level,
                                   Player player,
                                   BlockPos controllerPos,
                                   MultiblockPattern pattern,
                                   int southOffsetX,
                                   int southOffsetY,
                                   int southOffsetZ) {
        MultiblockBlock[][][] blocks = pattern.getPattern();
        BlockState controllerState = level.getBlockState(controllerPos);
        Direction controllerFacing = controllerState.getValue(HorizontalDirectionalBlock.FACING);
        List<Placement> placements = new ArrayList<>();

        for (int y = blocks.length - 1; y >= 0; y--) {
            for (int z = 0; z < blocks[y].length; z++) {
                for (int x = 0; x < blocks[y][z].length; x++) {
                    MultiblockBlock expected = blocks[y][z][x];
                    if (expected == null) {
                        continue;
                    }
                    if (placements.size() >= MAX_BUILD_BLOCKS) {
                        notify(player, "Structure is too large for the Smart Hammer");
                        return false;
                    }

                    BlockPos target = MultiblockPattern.rotateBlockPos(
                            controllerPos,
                            southOffsetX,
                            southOffsetY,
                            southOffsetZ,
                            blocks.length,
                            blocks[y].length,
                            x,
                            y,
                            z,
                            controllerState);
                    if (target.equals(controllerPos)) {
                        continue;
                    }

                    Map<String, String> states = rotateFacing(expected.getStates(), controllerFacing);
                    BlockState desired = applyBlockStates(expected.getBlock().defaultBlockState(), states);
                    BlockState current = level.getBlockState(target);
                    if (current.equals(desired)) {
                        continue;
                    }
                    if (!current.canBeReplaced()) {
                        notify(player, "Cannot replace " + current.getBlock().getName().getString() + " at " + target.toShortString());
                        return false;
                    }

                    Item requiredItem = requiredItem(expected.getBlock());
                    if (!player.isCreative() && requiredItem == Items.AIR) {
                        notify(player, "No placeable inventory item exists for " + expected.getBlock().getName().getString());
                        return false;
                    }
                    placements.add(new Placement(target, desired, requiredItem, expected.getBlock() == Blocks.WATER));
                }
            }
        }

        if (!player.isCreative() && !hasRequiredItems(player, placements)) {
            return false;
        }

        for (Placement placement : placements) {
            if (!player.isCreative()) {
                if (!consumeOne(player, placement.requiredItem)) {
                    notify(player, "Inventory changed while building; stopped safely");
                    return false;
                }
            }
            level.setBlock(placement.pos, placement.state, 3);
            if (!player.isCreative() && placement.water) {
                ItemStack bucket = new ItemStack(Items.BUCKET);
                if (!player.getInventory().add(bucket)) {
                    player.drop(bucket, false);
                }
            }
        }
        return true;
    }

    private static Item requiredItem(Block block) {
        if (block == Blocks.FARMLAND) {
            return Items.DIRT;
        }
        if (block == Blocks.WATER) {
            return Items.WATER_BUCKET;
        }
        return block.asItem();
    }

    private static boolean hasRequiredItems(Player player, List<Placement> placements) {
        Map<Item, Integer> required = new HashMap<>();
        for (Placement placement : placements) {
            required.merge(placement.requiredItem, 1, Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            int available = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(entry.getKey())) {
                    available += stack.getCount();
                }
            }
            if (available < entry.getValue()) {
                notify(player, "Missing " + (entry.getValue() - available) + " × " + entry.getKey().getDescription().getString());
                return false;
            }
        }
        return true;
    }

    private static boolean consumeOne(Player player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item) && !stack.isEmpty()) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> rotateFacing(Map<String, String> states, Direction controllerFacing) {
        if (states == null || !states.containsKey("facing")) {
            return states;
        }
        Direction authored = Direction.byName(states.get("facing"));
        if (authored == null || !authored.getAxis().isHorizontal()) {
            return states;
        }
        Map<String, String> rotated = new HashMap<>(states);
        rotated.put("facing", MultiblockPattern.rotateHorizontalDirection(controllerFacing, authored).getName());
        return rotated;
    }

    private static BlockState applyBlockStates(BlockState state, Map<String, String> states) {
        if (states == null) {
            return state;
        }
        BlockState result = state;
        StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
        for (Map.Entry<String, String> entry : states.entrySet()) {
            if ("operating".equals(entry.getKey())) {
                continue;
            }
            Property<?> property = definition.getProperty(entry.getKey());
            if (property != null) {
                result = applyState(result, property, entry.getValue());
            }
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState applyState(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }

    private static void notify(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    private record Placement(BlockPos pos, BlockState state, Item requiredItem, boolean water) {
    }
}
