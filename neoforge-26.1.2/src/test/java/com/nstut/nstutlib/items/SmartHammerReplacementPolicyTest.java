package com.nstut.nstutlib.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class SmartHammerReplacementPolicyTest {
    @org.junit.jupiter.api.BeforeAll
    static void bootstrapMinecraftRegistries() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        // ModDev's 26.1 test launcher does not bind vanilla item components.
        // Match the existing repository fixtures for the plain items used here.
        for (var item : new net.minecraft.world.item.Item[] {
                Items.GLASS, Items.STONE, Items.DIRT, Items.CHEST, Items.DIAMOND })
            item.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponents.COMMON_ITEM_COMPONENTS);
    }

    @Test void recoversGlassAndStoneAsBlocksAndFarmlandAsDirt() {
        assertTrue(inspect(Blocks.GLASS.defaultBlockState(),null,Set.of()).item().is(Items.GLASS));
        assertTrue(inspect(Blocks.STONE.defaultBlockState(),null,Set.of()).item().is(Items.STONE));
        assertTrue(inspect(Blocks.FARMLAND.defaultBlockState(),null,Set.of()).item().is(Items.DIRT));
        assertTrue(inspect(Blocks.AIR.defaultBlockState(),null,Set.of()).item().isEmpty());
    }
    @Test void creativeInfrastructureCannotBeReplaced() {
        for(var block: new net.minecraft.world.level.block.Block[]{Blocks.BEDROCK,Blocks.COMMAND_BLOCK,
                Blocks.REPEATING_COMMAND_BLOCK,Blocks.CHAIN_COMMAND_BLOCK,Blocks.BARRIER,Blocks.LIGHT,
                Blocks.STRUCTURE_BLOCK,Blocks.STRUCTURE_VOID,Blocks.JIGSAW,Blocks.END_PORTAL_FRAME})
            assertFalse(inspect(block.defaultBlockState(),null,Set.of()).allowed(), block.toString());
    }
    @Test void onlyEmptyVanillaContainersWithoutSavedMetadataAreSupported() {
        var state=Blocks.CHEST.defaultBlockState();
        var chest=new ChestBlockEntity(BlockPos.ZERO,state);
        var keys=Set.of("id","x","y","z","Items");
        assertTrue(inspect(state,chest,keys).allowed());
        chest.setItem(0,new ItemStack(Items.DIAMOND));
        assertFalse(inspect(state,chest,keys).allowed());
        chest.clearContent();
        assertFalse(inspect(state,chest,Set.of("id","Items","LootTable")).allowed());
        assertFalse(inspect(state,chest,Set.of("id","Items","CustomName")).allowed());
        assertFalse(inspect(state,chest,Set.of("id","Items","ForgeCaps")).allowed());
        assertFalse(inspect(state,chest,Set.of("id","Items","neoforge:attachments")).allowed());
    }
    @Test void supportedEntityCanVetoAndRecoveryPayloadIsCopied() {
        var state=Blocks.CHEST.defaultBlockState();
        var chest=new SupportedChest(state);
        var decision=inspect(state,chest,Set.of());
        assertTrue(decision.allowed(),decision.blocker());
        chest.setItem(0,new ItemStack(Items.DIAMOND));
        assertFalse(inspect(state,chest,Set.of()).allowed());
        var source=new ItemStack(Items.CHEST);
        var recovery=SmartHammerRecovery.ready(source);
        source.shrink(1);
        assertEquals(1,recovery.item().getCount());
        recovery.item().shrink(1);
        assertEquals(1,recovery.item().getCount());
    }
    @Test void unsupportedEntitiesAndMultiBlockStructuresStayBlocked() {
        var state=Blocks.FURNACE.defaultBlockState();
        assertFalse(inspect(state,new net.minecraft.world.level.block.entity.FurnaceBlockEntity(BlockPos.ZERO,state),Set.of()).allowed());
        assertFalse(inspect(Blocks.OAK_DOOR.defaultBlockState(),null,Set.of()).allowed());
        assertFalse(inspect(Blocks.PISTON_HEAD.defaultBlockState(),null,Set.of()).allowed());
        assertFalse(inspect(Blocks.CHEST.defaultBlockState(),null,Set.of()).allowed());
    }
    private static SmartHammerRecovery inspect(net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity entity,Set<String> keys) {
        return SmartHammerReplacementPolicy.inspect(null,BlockPos.ZERO,state,entity,keys,null);
    }
    private static class SupportedChest extends ChestBlockEntity implements SmartHammerRecoverable {
        SupportedChest(net.minecraft.world.level.block.state.BlockState state){super(BlockPos.ZERO,state);}
        @Override public SmartHammerRecovery prepareSmartHammerReplacement(net.minecraft.world.entity.player.Player player){
            return isEmpty()?SmartHammerRecovery.ready(new ItemStack(Items.CHEST)):SmartHammerRecovery.blocked("stored items");
        }
    }
}
