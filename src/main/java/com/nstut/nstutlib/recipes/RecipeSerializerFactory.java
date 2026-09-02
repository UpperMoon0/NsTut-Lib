package com.nstut.nstutlib.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RecipeSerializerFactory<T extends ModRecipe<T> & RecipeFactory<T>> {
    private static final int MAX_NETWORK_ENTRIES = 256;

    public RecipeSerializer<T> createSerializer(RecipeFactory<T> factory) {
        return new RecipeSerializer<T>() {
            @Override
            public @NotNull T fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
                try {
                    ModRecipeData data = readRecipeDataFromJson(json);
                    validate(data, recipeId.toString());
                    return factory.create(recipeId, data);
                } catch (RuntimeException exception) {
                    if (exception instanceof JsonParseException jsonParseException) {
                        throw jsonParseException;
                    }
                    throw new JsonParseException("Invalid recipe " + recipeId + ": " + exception.getMessage(), exception);
                }
            }

            @Override
            public T fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
                IngredientItem[] itemIngredients = readItemIngredientArray(buffer);
                OutputItem[] itemResults = readOutputItemArray(buffer);
                FluidStack[] fluidIngredients = readFluidStackArray(buffer);
                FluidStack[] fluidResults = readFluidStackArray(buffer);
                ModRecipeData data = new ModRecipeData(itemIngredients, itemResults, fluidIngredients, fluidResults, buffer.readInt());
                validate(data, recipeId.toString());
                return factory.create(recipeId, data);
            }

            @Override
            public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull T recipe) {
                ModRecipeData data = recipe.getRecipe();
                writeIngredientItemArray(buffer, data.getIngredientItems());
                writeOutputItemArray(buffer, data.getOutputItems());
                writeFluidStackArray(buffer, data.getFluidIngredients());
                writeFluidStackArray(buffer, data.getFluidOutputs());
                buffer.writeInt(data.getTotalEnergy());
            }
        };
    }

    private static ModRecipeData readRecipeDataFromJson(JsonObject json) {
        IngredientItem[] ingredientItems = readIngredientItemArrayFromJson(json.getAsJsonArray("itemInputs"));
        OutputItem[] outputItems = readOutputItemArrayFromJson(json.getAsJsonArray("itemOutputs"));
        FluidStack[] fluidIngredients = readFluidStackArrayFromJson(json.getAsJsonArray("fluidInputs"));
        FluidStack[] fluidResults = readFluidStackArrayFromJson(json.getAsJsonArray("fluidOutputs"));
        int totalEnergy = json.has("energy") ? json.get("energy").getAsInt() : 0;
        return new ModRecipeData(ingredientItems, outputItems, fluidIngredients, fluidResults, totalEnergy);
    }

    private static IngredientItem[] readItemIngredientArray(FriendlyByteBuf buffer) {
        int length = readBoundedLength(buffer, "item inputs");
        IngredientItem[] array = new IngredientItem[length];
        for (int i = 0; i < length; i++) {
            array[i] = new IngredientItem(buffer.readItem(), buffer.readBoolean());
        }
        return array;
    }

    private static OutputItem[] readOutputItemArray(FriendlyByteBuf buffer) {
        int length = readBoundedLength(buffer, "item outputs");
        OutputItem[] array = new OutputItem[length];
        for (int i = 0; i < length; i++) {
            array[i] = new OutputItem(buffer.readItem(), buffer.readFloat());
        }
        return array;
    }

    private static FluidStack[] readFluidStackArray(FriendlyByteBuf buffer) {
        int length = readBoundedLength(buffer, "fluids");
        FluidStack[] array = new FluidStack[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.readFluidStack();
        }
        return array;
    }

    private static int readBoundedLength(FriendlyByteBuf buffer, String name) {
        int length = buffer.readInt();
        if (length < 0 || length > MAX_NETWORK_ENTRIES) {
            throw new IllegalArgumentException("Invalid " + name + " count: " + length);
        }
        return length;
    }

    private static void writeIngredientItemArray(FriendlyByteBuf buffer, IngredientItem[] array) {
        buffer.writeInt(array.length);
        for (IngredientItem item : array) {
            buffer.writeItem(item.getItemStack());
            buffer.writeBoolean(item.isConsumable());
        }
    }

    private static void writeOutputItemArray(FriendlyByteBuf buffer, OutputItem[] array) {
        buffer.writeInt(array.length);
        for (OutputItem item : array) {
            buffer.writeItem(item.getItemStack());
            buffer.writeFloat(item.getChance());
        }
    }

    private static void writeFluidStackArray(FriendlyByteBuf buffer, FluidStack[] array) {
        buffer.writeInt(array.length);
        for (FluidStack fluid : array) {
            buffer.writeFluidStack(fluid);
        }
    }

    private static IngredientItem[] readIngredientItemArrayFromJson(JsonArray ingredientArray) {
        return Optional.ofNullable(ingredientArray)
                .map(array -> {
                    IngredientItem[] items = new IngredientItem[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        ItemStack stack = readItemStack(object.getAsJsonObject("itemStack"));
                        boolean consumable = !object.has("isConsumable") || object.get("isConsumable").getAsBoolean();
                        items[i] = new IngredientItem(stack, consumable);
                    }
                    return items;
                })
                .orElse(new IngredientItem[0]);
    }

    private static OutputItem[] readOutputItemArrayFromJson(JsonArray outputArray) {
        return Optional.ofNullable(outputArray)
                .map(array -> {
                    OutputItem[] items = new OutputItem[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        ItemStack stack = readItemStack(object.getAsJsonObject("itemStack"));
                        float chance = object.has("chance") ? object.get("chance").getAsFloat() : 1.0f;
                        items[i] = new OutputItem(stack, chance);
                    }
                    return items;
                })
                .orElse(new OutputItem[0]);
    }

    private static FluidStack[] readFluidStackArrayFromJson(JsonArray array) {
        return Optional.ofNullable(array)
                .map(jsonArray -> {
                    FluidStack[] fluids = new FluidStack[jsonArray.size()];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        fluids[i] = readFluidStack(jsonArray.get(i).getAsJsonObject());
                    }
                    return fluids;
                })
                .orElse(new FluidStack[0]);
    }

    private static ItemStack readItemStack(JsonObject json) {
        if (json == null) {
            throw new JsonParseException("Missing itemStack");
        }
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow(() -> new JsonParseException("Invalid itemStack: " + json))
                .getFirst();
    }

    private static FluidStack readFluidStack(JsonObject json) {
        if (json == null) {
            throw new JsonParseException("Missing fluid stack");
        }
        return FluidStack.CODEC.decode(JsonOps.INSTANCE, json)
                .result()
                .orElseThrow(() -> new JsonParseException("Invalid fluid stack: " + json))
                .getFirst();
    }

    private static void validate(ModRecipeData data, String recipeId) {
        if (data.getTotalEnergy() < 0) {
            throw new JsonParseException(recipeId + " has negative energy");
        }
        for (IngredientItem input : data.getIngredientItems()) {
            if (input == null || input.getItemStack().isEmpty() || input.getItemStack().getCount() <= 0) {
                throw new JsonParseException(recipeId + " has an invalid item input");
            }
        }
        for (OutputItem output : data.getOutputItems()) {
            if (output == null || output.getItemStack().isEmpty() || output.getItemStack().getCount() <= 0) {
                throw new JsonParseException(recipeId + " has an invalid item output");
            }
            if (!Float.isFinite(output.getChance()) || output.getChance() < 0.0f || output.getChance() > 1.0f) {
                throw new JsonParseException(recipeId + " has an invalid output chance " + output.getChance());
            }
        }
        for (FluidStack fluid : data.getFluidIngredients()) {
            if (fluid == null || fluid.isEmpty() || fluid.getAmount() <= 0) {
                throw new JsonParseException(recipeId + " has an invalid fluid input");
            }
        }
        for (FluidStack fluid : data.getFluidOutputs()) {
            if (fluid == null || fluid.isEmpty() || fluid.getAmount() <= 0) {
                throw new JsonParseException(recipeId + " has an invalid fluid output");
            }
        }
    }
}
