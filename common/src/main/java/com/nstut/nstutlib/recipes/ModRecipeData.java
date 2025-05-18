package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import dev.architectury.fluid.FluidStack;

import java.io.*;

@Getter
public class ModRecipeData implements Serializable {

    private final IngredientItem[] ingredientItems;
    private final OutputItem[] outputItems;
    private final IngredientFluid[] fluidIngredients;
    private final OutputFluid[] fluidOutputs;
    private final int totalEnergy;

    public ModRecipeData(IngredientItem[] inputs, OutputItem[] outputs, IngredientFluid[] fluidInputs, OutputFluid[] fluidOutputs, int totalEnergy) {
        this.ingredientItems = inputs;
        this.outputItems = outputs;
        this.fluidIngredients = fluidInputs;
        this.fluidOutputs = fluidOutputs;
        this.totalEnergy = totalEnergy;
    }

    public int getIngredientIndex(Item item) {
        for (int i = 0; i < ingredientItems.length; i++) {
            if (ingredientItems[i].getItemStack().getItem().equals(item)) {
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
        for (IngredientFluid fluidIngredient : fluidIngredients) {
            fluidIngredient.getFluidStack().write(buf);
            buf.writeBoolean(fluidIngredient.isConsumable());
        }
        buf.writeInt(fluidOutputs.length);
        for (OutputFluid fluidOutput : fluidOutputs) {
            fluidOutput.getFluidStack().write(buf);
            buf.writeFloat(fluidOutput.getChance());
        }
        buf.writeInt(totalEnergy);
    }

    public static ModRecipeData fromBuf(FriendlyByteBuf buf) {
        int ingredientItemCount = buf.readInt();
        IngredientItem[] ingredientItems = new IngredientItem[ingredientItemCount];
        for (int i = 0; i < ingredientItemCount; i++) {
            ingredientItems[i] = new IngredientItem(buf.readItem(), buf.readBoolean());
        }
        int outputItemCount = buf.readInt();
        OutputItem[] outputItems = new OutputItem[outputItemCount];
        for (int i = 0; i < outputItemCount; i++) {
            outputItems[i] = new OutputItem(buf.readItem(), buf.readFloat());
        }
        int fluidIngredientsLength = buf.readInt();
        IngredientFluid[] fluidIngredients = new IngredientFluid[fluidIngredientsLength];
        for (int i = 0; i < fluidIngredientsLength; i++) {
            fluidIngredients[i] = new IngredientFluid(FluidStack.read(buf), buf.readBoolean());
        }
        int fluidResultsLength = buf.readInt();
        OutputFluid[] fluidOutputs = new OutputFluid[fluidResultsLength];
        for (int i = 0; i < fluidResultsLength; i++) {
            fluidOutputs[i] = new OutputFluid(FluidStack.read(buf), buf.readFloat());
        }
        int totalEnergy = buf.readInt();
        return new ModRecipeData(ingredientItems, outputItems, fluidIngredients, fluidOutputs, totalEnergy);
    }
}
