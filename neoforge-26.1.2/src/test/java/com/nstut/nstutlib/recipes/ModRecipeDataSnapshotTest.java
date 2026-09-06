package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModRecipeDataSnapshotTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Items.DIAMOND.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
        Items.GOLD_INGOT.builtInRegistryHolder().bindComponents(DataComponents.COMMON_ITEM_COMPONENTS);
        Fluids.WATER.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
    }

    @Test
    void snapshotRetainsTemplatesAndCodecRoundTripsExactDefinition() {
        ModRecipeData original = new ModRecipeData(
                new IngredientItem[] {new IngredientItem(new ItemStack(Items.DIAMOND, 2), true)},
                new OutputItem[] {new OutputItem(new ItemStack(Items.GOLD_INGOT, 3), 0.5f)},
                new FluidStack[] {new FluidStack(Fluids.WATER, 750)},
                new FluidStack[] {new FluidStack(Fluids.WATER, 250)},
                1200);

        ModRecipeData snapshot = original.copy();
        original.getOutputItems()[0].setChance(0.1f);
        original.getFluidIngredients()[0].setAmount(1);

        assertEquals(2, snapshot.getIngredientItems()[0].getItemStackTemplate().count());
        assertEquals(3, snapshot.getOutputItems()[0].getItemStackTemplate().count());
        assertEquals(0.5f, snapshot.getOutputItems()[0].getChance());
        assertEquals(750, snapshot.getFluidIngredientTemplates()[0].amount());
        assertEquals(250, snapshot.getFluidOutputTemplates()[0].amount());
        assertEquals(1200, snapshot.getTotalEnergy());
        assertFalse(snapshot.getIngredientItems()[0] == original.getIngredientItems()[0]);

        var encoded = ModRecipeData.CODEC.encodeStart(NbtOps.INSTANCE, snapshot).result().orElseThrow();
        ModRecipeData decoded = ModRecipeData.CODEC.parse(NbtOps.INSTANCE, encoded).result().orElseThrow();
        assertEquals(2, decoded.getIngredientItems()[0].getItemStackTemplate().count());
        assertEquals(3, decoded.getOutputItems()[0].getItemStackTemplate().count());
        assertEquals(0.5f, decoded.getOutputItems()[0].getChance());
        assertEquals(750, decoded.getFluidIngredientTemplates()[0].amount());
        assertEquals(250, decoded.getFluidOutputTemplates()[0].amount());
        assertEquals(1200, decoded.getTotalEnergy());
    }
}
