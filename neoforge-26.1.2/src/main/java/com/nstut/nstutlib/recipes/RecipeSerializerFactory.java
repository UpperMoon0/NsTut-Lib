package com.nstut.nstutlib.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RecipeSerializerFactory<T extends ModRecipe<T> & RecipeFactory<T>> {
    private static final Identifier CODEC_PLACEHOLDER_ID =
            Identifier.fromNamespaceAndPath("nstutlib", "codec_decoded_recipe");

    private static final MapCodec<IngredientItem> INGREDIENT_ITEM_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("itemStack").forGetter(IngredientItem::getItemStack),
            Codec.BOOL.optionalFieldOf("isConsumable", false).forGetter(IngredientItem::isConsumable)
    ).apply(instance, IngredientItem::new));

    private static final MapCodec<OutputItem> OUTPUT_ITEM_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("itemStack").forGetter(OutputItem::getItemStack),
            Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(OutputItem::getChance)
    ).apply(instance, OutputItem::new));

    private static final MapCodec<ModRecipeData> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            INGREDIENT_ITEM_CODEC.codec().listOf().optionalFieldOf("itemInputs", List.of())
                    .forGetter(data -> Arrays.asList(data.getIngredientItems())),
            OUTPUT_ITEM_CODEC.codec().listOf().optionalFieldOf("itemOutputs", List.of())
                    .forGetter(data -> Arrays.asList(data.getOutputItems())),
            FluidStack.CODEC.listOf().optionalFieldOf("fluidInputs", List.of())
                    .forGetter(data -> Arrays.asList(data.getFluidIngredients())),
            FluidStack.CODEC.listOf().optionalFieldOf("fluidOutputs", List.of())
                    .forGetter(data -> Arrays.asList(data.getFluidOutputs())),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(ModRecipeData::getTotalEnergy)
    ).apply(instance, (itemInputs, itemOutputs, fluidInputs, fluidOutputs, energy) -> {
        ModRecipeData data = new ModRecipeData(
                itemInputs.toArray(IngredientItem[]::new),
                itemOutputs.toArray(OutputItem[]::new),
                fluidInputs.toArray(FluidStack[]::new),
                fluidOutputs.toArray(FluidStack[]::new),
                energy);
        validate(data, "decoded recipe");
        return data;
    }));

    public RecipeSerializer<T> createSerializer(RecipeFactory<T> factory) {
        MapCodec<T> codec = DATA_CODEC.xmap(
                data -> factory.create(CODEC_PLACEHOLDER_ID, data),
                ModRecipe::getRecipe);

        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull T decode(@NotNull RegistryFriendlyByteBuf buffer) {
                ModRecipeData data = ModRecipeData.fromBuf(buffer);
                validate(data, "network recipe");
                return factory.create(CODEC_PLACEHOLDER_ID, data);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull T recipe) {
                validate(recipe.getRecipe(), "network recipe");
                recipe.getRecipe().writeToBuf(buffer);
            }
        };

        return new RecipeSerializer<>(codec, streamCodec);
    }

    private static void validate(ModRecipeData data, String recipeId) {
        if (data.getTotalEnergy() < 0) throw new IllegalArgumentException(recipeId + " has negative energy");
        for (IngredientItem input : data.getIngredientItems()) {
            if (input == null || input.getItemStack().isEmpty() || input.getItemStack().getCount() <= 0) {
                throw new IllegalArgumentException(recipeId + " has an invalid item input");
            }
        }
        for (OutputItem output : data.getOutputItems()) {
            if (output == null || output.getItemStack().isEmpty() || output.getItemStack().getCount() <= 0) {
                throw new IllegalArgumentException(recipeId + " has an invalid item output");
            }
            if (!Float.isFinite(output.getChance()) || output.getChance() < 0.0f || output.getChance() > 1.0f) {
                throw new IllegalArgumentException(recipeId + " has an invalid output chance " + output.getChance());
            }
        }
        for (FluidStack fluid : data.getFluidIngredients()) {
            if (fluid == null || fluid.isEmpty() || fluid.getAmount() <= 0) {
                throw new IllegalArgumentException(recipeId + " has an invalid fluid input");
            }
        }
        for (FluidStack fluid : data.getFluidOutputs()) {
            if (fluid == null || fluid.isEmpty() || fluid.getAmount() <= 0) {
                throw new IllegalArgumentException(recipeId + " has an invalid fluid output");
            }
        }
    }
}
