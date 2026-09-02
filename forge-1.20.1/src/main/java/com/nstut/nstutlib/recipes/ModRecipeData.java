package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

@Getter
public final class ModRecipeData {
    private static final int MAX_NETWORK_ENTRIES = 256;

    private final IngredientItem[] ingredientItems;
    private final OutputItem[] outputItems;
    private final FluidStack[] fluidIngredients;
    private final FluidStack[] fluidOutputs;
    private final int totalEnergy;

    public ModRecipeData(IngredientItem[] inputs,
                         OutputItem[] outputs,
                         FluidStack[] fluidInputs,
                         FluidStack[] fluidOutputs,
                         int totalEnergy) {
        this.ingredientItems = inputs == null ? new IngredientItem[0] : Arrays.copyOf(inputs, inputs.length);
        this.outputItems = outputs == null ? new OutputItem[0] : Arrays.copyOf(outputs, outputs.length);
        this.fluidIngredients = copyFluids(fluidInputs);
        this.fluidOutputs = copyFluids(fluidOutputs);
        this.totalEnergy = totalEnergy;
    }

    private static FluidStack[] copyFluids(FluidStack[] fluids) {
        if (fluids == null) {
            return new FluidStack[0];
        }
        FluidStack[] copy = new FluidStack[fluids.length];
        for (int i = 0; i < fluids.length; i++) {
            copy[i] = fluids[i].copy();
        }
        return copy;
    }

    public int getIngredientIndex(Item item) {
        for (int i = 0; i < ingredientItems.length; i++) {
            if (ingredientItems[i].getItemStack().is(item)) {
                return i;
            }
        }
        return -1;
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeInt(ingredientItems.length);
        for (IngredientItem ingredientItem : ingredientItems) {
            buf.writeItem(ingredientItem.getItemStack());
            buf.writeBoolean(ingredientItem.isConsumable());
        }
        buf.writeInt(outputItems.length);
        for (OutputItem outputItem : outputItems) {
            buf.writeItem(outputItem.getItemStack());
            buf.writeFloat(outputItem.getChance());
        }
        buf.writeInt(fluidIngredients.length);
        for (FluidStack fluidStack : fluidIngredients) {
            buf.writeFluidStack(fluidStack);
        }
        buf.writeInt(fluidOutputs.length);
        for (FluidStack fluidStack : fluidOutputs) {
            buf.writeFluidStack(fluidStack);
        }
        buf.writeInt(totalEnergy);
    }

    public static ModRecipeData fromBuf(FriendlyByteBuf buf) {
        int ingredientCount = readBoundedCount(buf, "item inputs");
        IngredientItem[] ingredientItems = new IngredientItem[ingredientCount];
        for (int i = 0; i < ingredientCount; i++) {
            ingredientItems[i] = new IngredientItem(buf.readItem(), buf.readBoolean());
        }

        int outputCount = readBoundedCount(buf, "item outputs");
        OutputItem[] outputItems = new OutputItem[outputCount];
        for (int i = 0; i < outputCount; i++) {
            outputItems[i] = new OutputItem(buf.readItem(), buf.readFloat());
        }

        int fluidIngredientCount = readBoundedCount(buf, "fluid inputs");
        FluidStack[] fluidIngredients = new FluidStack[fluidIngredientCount];
        for (int i = 0; i < fluidIngredientCount; i++) {
            fluidIngredients[i] = buf.readFluidStack();
        }

        int fluidOutputCount = readBoundedCount(buf, "fluid outputs");
        FluidStack[] fluidOutputs = new FluidStack[fluidOutputCount];
        for (int i = 0; i < fluidOutputCount; i++) {
            fluidOutputs[i] = buf.readFluidStack();
        }

        return new ModRecipeData(ingredientItems, outputItems, fluidIngredients, fluidOutputs, buf.readInt());
    }

    private static int readBoundedCount(FriendlyByteBuf buf, String name) {
        int count = buf.readInt();
        if (count < 0 || count > MAX_NETWORK_ENTRIES) {
            throw new IllegalArgumentException("Invalid " + name + " count: " + count);
        }
        return count;
    }
}
