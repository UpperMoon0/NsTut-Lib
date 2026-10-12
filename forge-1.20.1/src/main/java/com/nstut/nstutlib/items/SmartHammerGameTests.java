package com.nstut.nstutlib.items;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NsTutLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SmartHammerGameTests {
    private SmartHammerGameTests() {}
    @GameTest(templateNamespace="forge",template="empty3x3x3",timeoutTicks=100)
    public static void replacesAndClearsWithExactRecovery(GameTestHelper helper) {
        var player=player(helper);
        var pos=helper.absolutePos(new BlockPos(1,1,1));
        helper.getLevel().setBlock(pos,Blocks.GLASS.defaultBlockState(),3);
        player.getInventory().add(new ItemStack(Items.BRICKS));
        helper.assertTrue(build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Replacement succeeds");
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.BRICKS),"New block placed");
        helper.assertTrue(count(player,Items.BRICKS)==0&&count(player,Items.GLASS)==1,"Consumes one brick, recovers glass once");
        helper.assertTrue(build(helper,player,pos,Blocks.AIR.defaultBlockState()),"Required air clears a block without an air item");
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir()&&count(player,Items.BRICKS)==1,"Cleared block is recovered");
        helper.succeed();
    }
    @GameTest(templateNamespace="forge",template="empty3x3x3",timeoutTicks=100)
    public static void noPartialBuildOnMissingMaterialsOrUnsupportedEntity(GameTestHelper helper) {
        var player=player(helper);var pos=helper.absolutePos(new BlockPos(1,1,1));var next=pos.east();
        var level=helper.getLevel();
        level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);level.setBlock(next,Blocks.STONE.defaultBlockState(),3);
        player.getInventory().add(new ItemStack(Items.BRICKS));
        helper.assertTrue(!buildTwo(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Insufficient materials block the entire plan");
        helper.assertTrue(level.getBlockState(pos).is(Blocks.STONE)&&level.getBlockState(next).is(Blocks.STONE)&&count(player,Items.BRICKS)==1,"No partial edit or consumption");
        level.setBlock(next,Blocks.FURNACE.defaultBlockState(),3);
        player.getInventory().add(new ItemStack(Items.BRICKS));
        helper.assertTrue(!buildTwo(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Unsupported entity blocks the entire plan");
        helper.assertTrue(level.getBlockState(pos).is(Blocks.STONE)&&level.getBlockState(next).is(Blocks.FURNACE)&&count(player,Items.BRICKS)==2,"Blocked entity and prior normal cell are untouched");
        helper.succeed();
    }
    @GameTest(templateNamespace="forge",template="empty3x3x3",timeoutTicks=100)
    public static void supportedEmptyContainerAndPopulatedContainer(GameTestHelper helper) {
        var player=player(helper);var pos=helper.absolutePos(new BlockPos(1,1,1));var level=helper.getLevel();
        level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);player.getInventory().add(new ItemStack(Items.BRICKS));
        helper.assertTrue(build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Empty vanilla chest can be recovered");
        helper.assertTrue(count(player,Items.CHEST)==1,"One chest returned");
        level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);
        var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(pos);
        chest.setItem(0,new ItemStack(Items.DIAMOND,3));player.getInventory().add(new ItemStack(Items.BRICKS));
        helper.assertTrue(!build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Populated chest is blocked");
        helper.assertTrue(level.getBlockEntity(pos)==chest&&chest.getItem(0).getCount()==3&&count(player,Items.BRICKS)==1,"Chest identity and stored diamonds remain intact");
        helper.succeed();
    }
    @GameTest(templateNamespace="forge",template="empty3x3x3",timeoutTicks=100)
    public static void protectedBlocksStayUntouchedEvenInCreative(GameTestHelper helper) {
        var player=player(helper);var pos=helper.absolutePos(new BlockPos(1,1,1));var level=helper.getLevel();
        for(boolean creative:new boolean[]{false,true}){
            ((net.minecraft.server.level.ServerPlayer)player).setGameMode(creative?net.minecraft.world.level.GameType.CREATIVE:net.minecraft.world.level.GameType.SURVIVAL);
            for(var block:new net.minecraft.world.level.block.Block[]{Blocks.BEDROCK,Blocks.COMMAND_BLOCK,Blocks.BARRIER,Blocks.STRUCTURE_BLOCK}){
                level.setBlock(pos,block.defaultBlockState(),3);
                player.getInventory().clearContent();player.getInventory().add(new ItemStack(Items.BRICKS));
                helper.assertTrue(!build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Protected infrastructure is blocked in both modes");
                helper.assertTrue(level.getBlockState(pos).is(block)&&count(player,Items.BRICKS)==1,"Protected block and material unchanged");
            }
        }
        ((net.minecraft.server.level.ServerPlayer)player).setGameMode(net.minecraft.world.level.GameType.SURVIVAL);helper.succeed();
    }
    @GameTest(templateNamespace="forge",template="empty3x3x3",timeoutTicks=100)
    public static void respectsBreakAndPlacementCancellationBeforeMutation(GameTestHelper helper) {
        var player=player(helper);var pos=helper.absolutePos(new BlockPos(1,1,1));var level=helper.getLevel();
        level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);player.getInventory().add(new ItemStack(Items.BRICKS));
        Object breakGuard=new Object(){
            @net.minecraftforge.eventbus.api.SubscribeEvent public void cancel(BlockEvent.BreakEvent event){if(event.getPos().equals(pos))event.setCanceled(true);}
        };
        MinecraftForge.EVENT_BUS.register(breakGuard);
        try{helper.assertTrue(!build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Break cancellation is respected");}
        finally{MinecraftForge.EVENT_BUS.unregister(breakGuard);}
        Object placeGuard=new Object(){
            @net.minecraftforge.eventbus.api.SubscribeEvent public void cancel(BlockEvent.EntityPlaceEvent event){
                if(event.getPos().equals(pos)){
                    helper.assertTrue(event.getPlacedBlock().is(Blocks.BRICKS)&&level.getBlockState(pos).is(Blocks.STONE),"Preflight reports desired block without world edits");
                    event.setCanceled(true);
                }
            }
        };
        MinecraftForge.EVENT_BUS.register(placeGuard);
        try{helper.assertTrue(!build(helper,player,pos,Blocks.BRICKS.defaultBlockState()),"Placement cancellation is respected");}
        finally{MinecraftForge.EVENT_BUS.unregister(placeGuard);}
        helper.assertTrue(level.getBlockState(pos).is(Blocks.STONE)&&count(player,Items.BRICKS)==1&&count(player,Items.STONE)==0,"No edits, consumption or refund from cancelled plans");
        helper.succeed();
    }
    private static Player player(GameTestHelper helper){
        var player=FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getInventory().clearContent();player.getAbilities().mayBuild=true;player.getAbilities().instabuild=false;
        return player;
    }
    private static boolean build(GameTestHelper helper,Player player,BlockPos target,BlockState desired){
        return buildPattern(helper,player,target,new MultiblockBlock[][][]{{{new MultiblockBlock(desired.getBlock())}}});
    }
    private static boolean buildTwo(GameTestHelper helper,Player player,BlockPos target,BlockState desired){
        return buildPattern(helper,player,target,new MultiblockBlock[][][]{{{new MultiblockBlock(desired.getBlock()),new MultiblockBlock(desired.getBlock())}}});
    }
    private static boolean buildPattern(GameTestHelper helper,Player player,BlockPos target,MultiblockBlock[][][] blocks){
        BlockPos anchor=target.west();
        helper.getLevel().setBlock(anchor,Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
        return new SmartHammer(new Item.Properties()).buildStructure(helper.getLevel(),player,anchor,new MultiblockPattern(blocks),-1,0,0);
    }
    private static int count(Player player,Item item){int count=0;for(var stack:player.getInventory().items)if(stack.is(item))count+=stack.getCount();return count;}
}
