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

    boolean buildStructure(Level level,
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
        int checked = 0;

        for (int y = blocks.length - 1; y >= 0; y--) for (int z = 0; z < blocks[y].length; z++) for (int x = 0; x < blocks[y][z].length; x++) {
            MultiblockBlock expected = blocks[y][z][x];
            if (expected == null) continue;
            if (++checked > MAX_BUILD_BLOCKS) { notify(player, "Structure is too large for the Smart Hammer"); return false; }
            BlockPos target = MultiblockPattern.rotateBlockPos(controllerPos, southOffsetX, southOffsetY, southOffsetZ,
                    blocks.length, blocks[y].length, x, y, z, controllerState);
            if (target.equals(controllerPos)) continue;
            if (!level.isLoaded(target) || !level.getWorldBorder().isWithinBounds(target)
                    || target.getY() < level.getMinBuildHeight() || target.getY() >= level.getMaxBuildHeight()) {
                notify(player, "Blocked by unavailable position at " + target.toShortString()); return false;
            }
            BlockState current = level.getBlockState(target);
            if (MultiblockPattern.matchesBlock(expected, current, controllerState)) continue;
            BlockState desired = applyBlockStates(expected.getBlock().defaultBlockState(), rotateFacing(expected.getStates(), controllerFacing));
            if (SmartHammerReplacementPolicy.resourceBearingState(desired)) {
                blocked(player, desired, target, "unsupported resource-bearing pattern state"); return false;
            }
            if (SmartHammerReplacementPolicy.protectedBlock(level, target, desired)) {
                blocked(player, desired, target, "protected pattern block"); return false;
            }
            if (desired.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                    || desired.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock
                    || desired.getBlock() == Blocks.PISTON_HEAD) {
                blocked(player, desired, target, "unsupported multi-block placement"); return false;
            }
            BlockEntity entity = level.getBlockEntity(target);
            SmartHammerRecovery recovery = recovery(level, target, current, entity, player);
            if (!recovery.allowed()) { blocked(player, current, target, recovery.blocker()); return false; }
            if (!allowedEdit(level, player, target, current, desired)) {
                blocked(player, current, target, "permission denied"); return false;
            }
            Item required = desired.isAir() || (desired.getBlock() == current.getBlock() && desired.getBlock() != Blocks.WATER)
                    ? Items.AIR : requiredItem(desired.getBlock());
            if (!desired.isAir() && desired.getBlock() != current.getBlock() && required == Items.AIR) {
                blocked(player, desired, target, "no placeable inventory item"); return false;
            }
            ItemStack returned = desired.getBlock() == current.getBlock() ? ItemStack.EMPTY : recovery.item();
            placements.add(new Placement(target, desired, required, desired.getBlock() == Blocks.WATER && required != Items.AIR,
                    current, entity, returned, expected));
        }
        // Every blocker, protection event and material check runs before the first tool mutation.
        if (!player.isCreative() && !hasRequiredItems(player, placements)) return false;

        for (Placement placement : placements) {
            BlockState liveState = level.getBlockState(placement.pos);
            // Earlier placements may legitimately change fence/pipe/stair neighbour properties.
            BlockState updatedPrevious = Block.updateFromNeighbourShapes(placement.previous, level, placement.pos);
            if ((!liveState.equals(placement.previous) && !liveState.equals(updatedPrevious)) || level.getBlockEntity(placement.pos) != placement.entity) {
                blocked(player, level.getBlockState(placement.pos), placement.pos, "world changed while building"); return false;
            }
            SmartHammerRecovery currentRecovery = recovery(level, placement.pos, liveState, placement.entity, player);
            ItemStack currentReturn = placement.state.getBlock() == placement.previous.getBlock() ? ItemStack.EMPTY : currentRecovery.item();
            if (!currentRecovery.allowed() || !samePayload(currentReturn, placement.returned)) {
                blocked(player, placement.previous, placement.pos, "stored data changed while building"); return false;
            }
            if (!player.getAbilities().mayBuild || !level.mayInteract(player, placement.pos)) {
                blocked(player, placement.previous, placement.pos, "permission denied"); return false;
            }
            boolean consumed = !player.isCreative() && placement.requiredItem != Items.AIR;
            if (consumed && !consumeOne(player, placement.requiredItem)) {
                notify(player, "Inventory changed while building; stopped safely"); return false;
            }
            var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, placement.pos);
            BlockState connectedState = Block.updateFromNeighbourShapes(placement.state, level, placement.pos);
            if (!level.setBlock(placement.pos, connectedState, 3) || !MultiblockPattern.matchesBlock(placement.expected, level.getBlockState(placement.pos), controllerState)) {
                boolean restored = snapshot.restore(3);
                if (consumed && restored) refundOne(player, placement.requiredItem);
                notify(player, "Could not replace block at " + placement.pos.toShortString()); return false;
            }
            returnItem(player, placement.returned);
            if (!player.isCreative() && placement.water) returnItem(player, new ItemStack(Items.BUCKET));
        }
        return true;
    }

    private static SmartHammerRecovery recovery(Level level, BlockPos pos, BlockState state, BlockEntity entity, Player player) {
        java.util.Set<String> keys = null;
        if (entity != null && (entity.getClass() == net.minecraft.world.level.block.entity.ChestBlockEntity.class
                || entity.getClass() == net.minecraft.world.level.block.entity.BarrelBlockEntity.class))
            keys = entity.saveWithFullMetadata(level.registryAccess()).getAllKeys();
        return SmartHammerReplacementPolicy.inspect(level, pos, state, entity, keys, player);
    }

    private static boolean allowedEdit(Level level, Player player, BlockPos pos, BlockState current, BlockState desired) {
        if (!player.getAbilities().mayBuild || !level.mayInteract(player, pos)) return false;
        if (!current.isAir()) {
            var event = new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level, pos, current, player);
            if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        }
        if (!desired.isAir()) {
            var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, pos);
            var event = new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(pos.below()), player) {
                @Override public BlockState getState() { return desired; }
                @Override public BlockState getPlacedBlock() { return desired; }
            };
            if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        }
        return true;
    }

    private static boolean samePayload(ItemStack a, ItemStack b) {
        return ItemStack.matches(a, b);
    }

    private static boolean plain(ItemStack stack, Item item) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
    }

    private static void returnItem(Player player, ItemStack item) {
        ItemStack copy = item.copy();
        if (!copy.isEmpty() && !player.getInventory().add(copy)) player.drop(copy, false);
    }

    private static void blocked(Player player, BlockState state, BlockPos pos, String reason) {
        notify(player, "Blocked by " + state.getBlock().getName().getString() + " at " + pos.toShortString() + ": " + reason);
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
            if (placement.requiredItem != Items.AIR) required.merge(placement.requiredItem, 1, Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            int available = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (plain(stack, entry.getKey())) {
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
            if (plain(stack, item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void refundOne(Player player, Item item) {
        ItemStack refund = new ItemStack(item);
        if (!player.getInventory().add(refund)) {
            player.drop(refund, false);
        }
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

    private record Placement(BlockPos pos, BlockState state, Item requiredItem, boolean water,
                             BlockState previous, BlockEntity entity, ItemStack returned, MultiblockBlock expected) {}
}
