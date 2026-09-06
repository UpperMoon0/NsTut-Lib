package com.nstut.nstutlib.models;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;

@GameTestHolder(NsTutLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StructureScannerGameTests {
    private StructureScannerGameTests() {
    }

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 100)
    public static void leafDistanceIsRuntimeManaged(GameTestHelper helper) {
        Map<String, String> leaves = StructureScannerStateFilter.exportStates(Blocks.OAK_LEAVES.defaultBlockState());
        helper.assertTrue(!leaves.containsKey("distance"),
                "Structure Scanner must not persist vanilla leaf decay distance");
        helper.assertTrue(leaves.containsKey("persistent"),
                "Structure Scanner must preserve authored leaf persistence");
        helper.assertTrue(leaves.containsKey("waterlogged"),
                "Structure Scanner must preserve authored leaf waterlogging");
        helper.succeed();
    }
}
