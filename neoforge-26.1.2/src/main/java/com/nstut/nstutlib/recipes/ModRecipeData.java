package com.nstut.nstutlib.recipes;

import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.Arrays;

@Getter
public final class ModRecipeData {
    private static final int MAX_NETWORK_ENTRIES = 256;

    private final IngredientItem[] ingredientItems;
    private final OutputItem[] outputItems;
    private final FluidStackTemplate[] fluidIngredientTemplates;
    private final FluidStackTemplate[] fluidOutputTemplates;
    private final int totalEnergy;

    private transient FluidStack[] fluidIngredients;
    private transient FluidStack[] fluidOutputs;

    public ModRecipeData(IngredientItem[] inputs,
                         OutputItem[] outputs,
                         FluidStack[] fluidInputs,
                         FluidStack[] fluidOutputs,
                         int totalEnergy) {
        this(inputs, outputs, toFluidTemplates(fluidInputs), toFluidTemplates(fluidOutputs), totalEnergy);
        this.fluidIngredients = copyFluids(fluidInputs);
        this.fluidOutputs = copyFluids(fluidOutputs);
    }

    public ModRecipeData(IngredientItem[] inputs,
                         OutputItem[] outputs,
                         FluidStackTemplate[] fluidInputs,
                         FluidStackTemplate[] fluidOutputs,
                         int totalEnergy) {
        this.ingredientItems = inputs == null ? new IngredientItem[0] : Arrays.copyOf(inputs, inputs.length);
        this.outputItems = outputs == null ? new OutputItem[0] : Arrays.copyOf(outputs, outputs.length);
        this.fluidIngredientTemplates = copyFluidTemplates(fluidInputs);
        this.fluidOutputTemplates = copyFluidTemplates(fluidOutputs);
        this.totalEnergy = totalEnergy;
    }

    public FluidStack[] getFluidIngredients() {
        if (fluidIngredients == null) fluidIngredients = createFluids(fluidIngredientTemplates);
        return fluidIngredients;
    }

    public FluidStack[] getFluidOutputs() {
        if (fluidOutputs == null) fluidOutputs = createFluids(fluidOutputTemplates);
        return fluidOutputs;
    }

    private static FluidStack[] copyFluids(FluidStack[] fluids) {
        if (fluids == null) return new FluidStack[0];
        FluidStack[] copy = new FluidStack[fluids.length];
        for (int i = 0; i < fluids.length; i++) {
            copy[i] = fluids[i] == null ? FluidStack.EMPTY : fluids[i].copy();
        }
        return copy;
    }

    private static FluidStackTemplate[] copyFluidTemplates(FluidStackTemplate[] fluids) {
        return fluids == null ? new FluidStackTemplate[0] : Arrays.copyOf(fluids, fluids.length);
    }

    private static FluidStackTemplate[] toFluidTemplates(FluidStack[] fluids) {
        if (fluids == null) return new FluidStackTemplate[0];
        FluidStackTemplate[] templates = new FluidStackTemplate[fluids.length];
        for (int i = 0; i < fluids.length; i++) {
            FluidStack fluid = fluids[i];
            templates[i] = fluid == null || fluid.isEmpty() ? null : FluidStackTemplate.fromNonEmptyStack(fluid);
        }
        return templates;
    }

    private static FluidStack[] createFluids(FluidStackTemplate[] templates) {
        FluidStack[] fluids = new FluidStack[templates.length];
        for (int i = 0; i < templates.length; i++) {
            fluids[i] = templates[i] == null ? FluidStack.EMPTY : templates[i].create();
        }
        return fluids;
    }

    public int getIngredientIndex(Item item) {
        for (int i = 0; i < ingredientItems.length; i++) {
            if (ingredientItems[i].getItemStack().is(item)) return i;
        }
        return -1;
    }

    public void writeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeInt(ingredientItems.length);
        for (IngredientItem ingredientItem : ingredientItems) {
            ItemStackTemplate template = ingredientItem.getItemStackTemplate();
            if (template == null) throw new IllegalArgumentException("Cannot encode an empty recipe item input");
            ItemStackTemplate.STREAM_CODEC.encode(buf, template);
            buf.writeBoolean(ingredientItem.isConsumable());
        }
        buf.writeInt(outputItems.length);
        for (OutputItem outputItem : outputItems) {
            ItemStackTemplate template = outputItem.getItemStackTemplate();
            if (template == null) throw new IllegalArgumentException("Cannot encode an empty recipe item output");
            ItemStackTemplate.STREAM_CODEC.encode(buf, template);
            buf.writeFloat(outputItem.getChance());
        }
        buf.writeInt(fluidIngredientTemplates.length);
        for (FluidStackTemplate fluid : fluidIngredientTemplates) {
            if (fluid == null) throw new IllegalArgumentException("Cannot encode an empty recipe fluid input");
            FluidStackTemplate.STREAM_CODEC.encode(buf, fluid);
        }
        buf.writeInt(fluidOutputTemplates.length);
        for (FluidStackTemplate fluid : fluidOutputTemplates) {
            if (fluid == null) throw new IllegalArgumentException("Cannot encode an empty recipe fluid output");
            FluidStackTemplate.STREAM_CODEC.encode(buf, fluid);
        }
        buf.writeInt(totalEnergy);
    }

    public static ModRecipeData fromBuf(RegistryFriendlyByteBuf buf) {
        int ingredientCount = readBoundedCount(buf, "item inputs");
        IngredientItem[] ingredientItems = new IngredientItem[ingredientCount];
        for (int i = 0; i < ingredientCount; i++) {
            ingredientItems[i] = new IngredientItem(ItemStackTemplate.STREAM_CODEC.decode(buf), buf.readBoolean());
        }

        int outputCount = readBoundedCount(buf, "item outputs");
        OutputItem[] outputItems = new OutputItem[outputCount];
        for (int i = 0; i < outputCount; i++) {
            outputItems[i] = new OutputItem(ItemStackTemplate.STREAM_CODEC.decode(buf), buf.readFloat());
        }

        int fluidIngredientCount = readBoundedCount(buf, "fluid inputs");
        FluidStackTemplate[] fluidIngredients = new FluidStackTemplate[fluidIngredientCount];
        for (int i = 0; i < fluidIngredientCount; i++) {
            fluidIngredients[i] = FluidStackTemplate.STREAM_CODEC.decode(buf);
        }

        int fluidOutputCount = readBoundedCount(buf, "fluid outputs");
        FluidStackTemplate[] fluidOutputs = new FluidStackTemplate[fluidOutputCount];
        for (int i = 0; i < fluidOutputCount; i++) {
            fluidOutputs[i] = FluidStackTemplate.STREAM_CODEC.decode(buf);
        }

        return new ModRecipeData(ingredientItems, outputItems, fluidIngredients, fluidOutputs, buf.readInt());
    }

    private static int readBoundedCount(RegistryFriendlyByteBuf buf, String name) {
        int count = buf.readInt();
        if (count < 0 || count > MAX_NETWORK_ENTRIES) {
            throw new IllegalArgumentException("Invalid " + name + " count: " + count);
        }
        return count;
    }
}
