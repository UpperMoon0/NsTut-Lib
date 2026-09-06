package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
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
    }

    @Test
    void snapshotIsDeepAndCodecRoundTripsExactDefinition() {
        ModRecipeData original = new ModRecipeData(
                new IngredientItem[] {new IngredientItem(new ItemStack(Items.DIAMOND, 2), true)},
                new OutputItem[] {new OutputItem(new ItemStack(Items.GOLD_INGOT, 3), 0.5f)},
                new FluidStack[] {new FluidStack(Fluids.WATER, 750)},
                new FluidStack[] {new FluidStack(Fluids.WATER, 250)},
                1200);

        ModRecipeData snapshot = original.copy();
        original.getIngredientItems()[0].getItemStack().setCount(9);
        original.getOutputItems()[0].getItemStack().setCount(8);
        original.getOutputItems()[0].setChance(0.1f);
        original.getFluidIngredients()[0].setAmount(1);

        assertEquals(2, snapshot.getIngredientItems()[0].getItemStack().getCount());
        assertEquals(3, snapshot.getOutputItems()[0].getItemStack().getCount());
        assertEquals(0.5f, snapshot.getOutputItems()[0].getChance());
        assertEquals(750, snapshot.getFluidIngredients()[0].getAmount());
        assertEquals(1200, snapshot.getTotalEnergy());
        assertFalse(snapshot.getIngredientItems()[0] == original.getIngredientItems()[0]);

        var encoded = ModRecipeData.CODEC.encodeStart(NbtOps.INSTANCE, snapshot).result().orElseThrow();
        ModRecipeData decoded = ModRecipeData.CODEC.parse(NbtOps.INSTANCE, encoded).result().orElseThrow();
        assertEquals(2, decoded.getIngredientItems()[0].getItemStack().getCount());
        assertEquals(3, decoded.getOutputItems()[0].getItemStack().getCount());
        assertEquals(0.5f, decoded.getOutputItems()[0].getChance());
        assertEquals(750, decoded.getFluidIngredients()[0].getAmount());
        assertEquals(250, decoded.getFluidOutputs()[0].getAmount());
        assertEquals(1200, decoded.getTotalEnergy());
    }
}
