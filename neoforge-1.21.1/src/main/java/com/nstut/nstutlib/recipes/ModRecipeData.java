package com.nstut.nstutlib.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

@Getter
public final class ModRecipeData {
    private static final int MAX_NETWORK_ENTRIES = 256;

    private static final Codec<IngredientItem> INGREDIENT_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("itemStack").forGetter(IngredientItem::getItemStack),
            Codec.BOOL.fieldOf("isConsumable").forGetter(IngredientItem::isConsumable)
    ).apply(instance, IngredientItem::new));

    private static final Codec<OutputItem> OUTPUT_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("itemStack").forGetter(OutputItem::getItemStack),
            Codec.FLOAT.fieldOf("chance").forGetter(OutputItem::getChance)
    ).apply(instance, OutputItem::new));

    /**
     * Persistence codec for the exact definition of an in-flight machine recipe.
     * Keeping this independent of the recipe manager prevents datapack reloads from
     * changing a transaction after its inputs have already been consumed.
     */
    public static final Codec<ModRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            INGREDIENT_ITEM_CODEC.listOf().fieldOf("itemInputs")
                    .forGetter(data -> Arrays.asList(data.ingredientItems)),
            OUTPUT_ITEM_CODEC.listOf().fieldOf("itemOutputs")
                    .forGetter(data -> Arrays.asList(data.outputItems)),
            FluidStack.CODEC.listOf().fieldOf("fluidInputs")
                    .forGetter(data -> Arrays.asList(data.fluidIngredients)),
            FluidStack.CODEC.listOf().fieldOf("fluidOutputs")
                    .forGetter(data -> Arrays.asList(data.fluidOutputs)),
            Codec.INT.fieldOf("energy").forGetter(ModRecipeData::getTotalEnergy)
    ).apply(instance, ModRecipeData::fromCodecLists));

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

    private static ModRecipeData fromCodecLists(List<IngredientItem> inputs,
                                                List<OutputItem> outputs,
                                                List<FluidStack> fluidInputs,
                                                List<FluidStack> fluidOutputs,
                                                int totalEnergy) {
        requireBounded(inputs, "item inputs");
        requireBounded(outputs, "item outputs");
        requireBounded(fluidInputs, "fluid inputs");
        requireBounded(fluidOutputs, "fluid outputs");
        return new ModRecipeData(
                inputs.toArray(IngredientItem[]::new),
                outputs.toArray(OutputItem[]::new),
                fluidInputs.toArray(FluidStack[]::new),
                fluidOutputs.toArray(FluidStack[]::new),
                totalEnergy);
    }

    private static void requireBounded(List<?> values, String name) {
        if (values.size() > MAX_NETWORK_ENTRIES) {
            throw new IllegalArgumentException("Invalid " + name + " count: " + values.size());
        }
    }

    /** Returns a transaction-owned deep snapshot of this recipe definition. */
    public ModRecipeData copy() {
        IngredientItem[] inputs = new IngredientItem[ingredientItems.length];
        for (int i = 0; i < ingredientItems.length; i++) {
            IngredientItem input = ingredientItems[i];
            inputs[i] = input == null ? null : new IngredientItem(input.getItemStack().copy(), input.isConsumable());
        }
        OutputItem[] outputs = new OutputItem[outputItems.length];
        for (int i = 0; i < outputItems.length; i++) {
            OutputItem output = outputItems[i];
            outputs[i] = output == null ? null : new OutputItem(output.getItemStack().copy(), output.getChance());
        }
        return new ModRecipeData(inputs, outputs, fluidIngredients, fluidOutputs, totalEnergy);
    }

    private static FluidStack[] copyFluids(FluidStack[] fluids) {
        if (fluids == null) return new FluidStack[0];
        FluidStack[] copy = new FluidStack[fluids.length];
        for (int i = 0; i < fluids.length; i++) copy[i] = fluids[i].copy();
        return copy;
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
            ItemStack.STREAM_CODEC.encode(buf, ingredientItem.getItemStack());
            buf.writeBoolean(ingredientItem.isConsumable());
        }
        buf.writeInt(outputItems.length);
        for (OutputItem outputItem : outputItems) {
            ItemStack.STREAM_CODEC.encode(buf, outputItem.getItemStack());
            buf.writeFloat(outputItem.getChance());
        }
        buf.writeInt(fluidIngredients.length);
        for (FluidStack fluidStack : fluidIngredients) FluidStack.STREAM_CODEC.encode(buf, fluidStack);
        buf.writeInt(fluidOutputs.length);
        for (FluidStack fluidStack : fluidOutputs) FluidStack.STREAM_CODEC.encode(buf, fluidStack);
        buf.writeInt(totalEnergy);
    }

    public static ModRecipeData fromBuf(RegistryFriendlyByteBuf buf) {
        int ingredientCount = readBoundedCount(buf, "item inputs");
        IngredientItem[] ingredientItems = new IngredientItem[ingredientCount];
        for (int i = 0; i < ingredientCount; i++) {
            ingredientItems[i] = new IngredientItem(ItemStack.STREAM_CODEC.decode(buf), buf.readBoolean());
        }

        int outputCount = readBoundedCount(buf, "item outputs");
        OutputItem[] outputItems = new OutputItem[outputCount];
        for (int i = 0; i < outputCount; i++) {
            outputItems[i] = new OutputItem(ItemStack.STREAM_CODEC.decode(buf), buf.readFloat());
        }

        int fluidIngredientCount = readBoundedCount(buf, "fluid inputs");
        FluidStack[] fluidIngredients = new FluidStack[fluidIngredientCount];
        for (int i = 0; i < fluidIngredientCount; i++) fluidIngredients[i] = FluidStack.STREAM_CODEC.decode(buf);

        int fluidOutputCount = readBoundedCount(buf, "fluid outputs");
        FluidStack[] fluidOutputs = new FluidStack[fluidOutputCount];
        for (int i = 0; i < fluidOutputCount; i++) fluidOutputs[i] = FluidStack.STREAM_CODEC.decode(buf);

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
