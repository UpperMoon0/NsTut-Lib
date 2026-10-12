package com.nstut.nstutlib.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Set;

/** Shared default-deny replacement policy, used identically by every loader. */
public final class SmartHammerReplacementPolicy {
    private SmartHammerReplacementPolicy() {}
    public static boolean protectedBlock(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        return block instanceof GameMasterBlock || state.getDestroySpeed(level, pos) < 0
                || block == Blocks.BEDROCK || block == Blocks.BARRIER || block == Blocks.LIGHT
                || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK
                || block == Blocks.REPEATING_COMMAND_BLOCK || block == Blocks.STRUCTURE_BLOCK
                || block == Blocks.STRUCTURE_VOID || block == Blocks.JIGSAW
                || block == Blocks.END_PORTAL || block == Blocks.END_PORTAL_FRAME
                || block == Blocks.END_GATEWAY || block == Blocks.MOVING_PISTON;
    }
    /** States encoding stored resources are deliberately unsupported in either direction.
     * A single block item cannot represent these quantities (or consumed food/charge).
     * Check property names too so loader-specific and modded variants fail closed.
     */
    public static boolean resourceBearingState(BlockState state) {
        if (state.getBlock() instanceof SlabBlock
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
                    == net.minecraft.world.level.block.state.properties.SlabType.DOUBLE) return true;
        for (var property : state.getProperties()) {
            if (property.getName().equals("level") && !(state.getBlock() instanceof LiquidBlock)) return true;
            if (Set.of("candles", "pickles", "eggs", "layers", "bites", "charges", "honey_level",
                    "flower_amount", "segment_amount", "dusted").contains(property.getName())) return true;
        }
        return false;
    }
    public static SmartHammerRecovery inspect(Level level, BlockPos pos, BlockState state,
                                              BlockEntity entity, Set<String> savedKeys, Player player) {
        if (resourceBearingState(state)) return SmartHammerRecovery.blocked("unsupported resource-bearing state");
        if (protectedBlock(level, pos, state)) return SmartHammerRecovery.blocked("protected block");
        if (entity != null) {
            if (entity instanceof SmartHammerRecoverable support) {
                SmartHammerRecovery decision;
                try { decision = support.prepareSmartHammerReplacement(player); }
                catch (RuntimeException failure) { return SmartHammerRecovery.blocked("replacement support failed"); }
                if (decision == null) return SmartHammerRecovery.blocked("support returned no recovery decision");
                if (!decision.allowed()) return decision;
                ItemStack item = decision.item();
                if (item.isEmpty() || !item.is(state.getBlock().asItem()) || item.getCount() != 1)
                    return SmartHammerRecovery.blocked("support returned an invalid recovery item");
                return decision;
            }
            boolean vanilla = entity.getClass() == ChestBlockEntity.class || entity.getClass() == BarrelBlockEntity.class;
            if (vanilla && savedKeys != null && Set.of("id","x","y","z","Items").containsAll(savedKeys)
                    && ((Container) entity).isEmpty())
                return SmartHammerRecovery.ready(new ItemStack(state.getBlock()));
            return SmartHammerRecovery.blocked(vanilla ? "container has contents or saved data" : "unsupported block entity");
        }
        // These blocks can remove a partner/base during neighbor updates: no single-cell recovery.
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof DoublePlantBlock
                || state.getBlock() == Blocks.PISTON_HEAD)
            return SmartHammerRecovery.blocked("unsupported multi-block structure");
        if (state.hasBlockEntity()) return SmartHammerRecovery.blocked("missing block entity");
        if (state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.DIRT_PATH)
            return SmartHammerRecovery.ready(new ItemStack(Items.DIRT));
        if (state.isAir() || state.getBlock().asItem() == Items.AIR)
            return SmartHammerRecovery.ready(ItemStack.EMPTY);
        return SmartHammerRecovery.ready(new ItemStack(state.getBlock()));
    }
}
