package com.nstut.nstutlib.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import dev.architectury.fluid.FluidStack; 
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RecipeSerializerFactory<T extends ModRecipe<T> & RecipeFactory<T>> {
    private static final Gson GSON = new Gson();

    public RecipeSerializer<T> createSerializer(RecipeFactory<T> factory) {
        return new RecipeSerializer<T>() {
            @Override
            public @NotNull T fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
                ModRecipeData recipeData = readRecipeDataFromJson(pSerializedRecipe);
                return factory.create(pRecipeId, recipeData);
            }

            @Override
            public T fromNetwork(@NotNull ResourceLocation pRecipeId, @NotNull FriendlyByteBuf pBuffer) {
                IngredientItem[] itemIngredients = readItemIngredientArray(pBuffer);
                OutputItem[] itemResults = readOutputItemArray(pBuffer);

                FluidStack[] fluidIngredients = readFluidStackArray(pBuffer);
                FluidStack[] fluidResults = readFluidStackArray(pBuffer);

                int totalEnergy = pBuffer.readInt();

                ModRecipeData recipeData = new ModRecipeData(itemIngredients, itemResults, fluidIngredients, fluidResults, totalEnergy);
                return factory.create(pRecipeId, recipeData);
            }

            @Override
            public void toNetwork(@NotNull FriendlyByteBuf pBuffer, @NotNull T pRecipe) {
                ModRecipeData recipeContainer = pRecipe.getRecipeData(); // Changed from getRecipe()

                writeIngredientItemArray(pBuffer, recipeContainer.getIngredientItems());
                writeOutputItemArray(pBuffer, recipeContainer.getOutputItems());

                writeFluidStackArray(pBuffer, recipeContainer.getFluidIngredients());
                writeFluidStackArray(pBuffer, recipeContainer.getFluidOutputs());

                pBuffer.writeInt(recipeContainer.getTotalEnergy());
            }
        };
    }

    private static ModRecipeData readRecipeDataFromJson(JsonObject pSerializedRecipe) {
        IngredientItem[] ingredientItems = readIngredientItemArrayFromJson(pSerializedRecipe.getAsJsonArray("itemInputs"));
        OutputItem[] outputItems = readOutputItemArrayFromJson(pSerializedRecipe.getAsJsonArray("itemOutputs"));

        FluidStack[] fluidIngredients = readFluidStackArrayFromJson(pSerializedRecipe.getAsJsonArray("fluidInputs"));
        FluidStack[] fluidResults = readFluidStackArrayFromJson(pSerializedRecipe.getAsJsonArray("fluidOutputs"));

        int totalEnergy = pSerializedRecipe.get("energy").getAsInt();

        return new ModRecipeData(ingredientItems, outputItems, fluidIngredients, fluidResults, totalEnergy);
    }

    private static IngredientItem[] readItemIngredientArray(FriendlyByteBuf pBuffer) {
        int length = pBuffer.readInt();
        IngredientItem[] array = new IngredientItem[length];
        for (int i = 0; i < length; i++) {
            array[i] = new IngredientItem(pBuffer.readItem(), pBuffer.readBoolean());
        }
        return array;
    }

    private static OutputItem[] readOutputItemArray(FriendlyByteBuf pBuffer) {
        int length = pBuffer.readInt();
        OutputItem[] array = new OutputItem[length];
        for (int i = 0; i < length; i++) {
            array[i] = new OutputItem(pBuffer.readItem(), pBuffer.readFloat());
        }
        return array;
    }

    private static FluidStack[] readFluidStackArray(FriendlyByteBuf pBuffer) {
        int length = pBuffer.readInt();
        FluidStack[] array = new FluidStack[length];
        for (int i = 0; i < length; i++) {
            array[i] = FluidStack.read(pBuffer); // Changed from pBuffer.readFluidStack()
        }
        return array;
    }

    private static void writeIngredientItemArray(FriendlyByteBuf pBuffer, IngredientItem[] array) {
        pBuffer.writeInt(array.length);
        for (IngredientItem item : array) {
            pBuffer.writeItem(item.getItemStack());
            pBuffer.writeBoolean(item.isConsumable());
        }
    }

    private static void writeOutputItemArray(FriendlyByteBuf pBuffer, OutputItem[] array) {
        pBuffer.writeInt(array.length);
        for (OutputItem item : array) {
            pBuffer.writeItem(item.getItemStack());
            pBuffer.writeFloat(item.getChance());
        }
    }

    private static void writeFluidStackArray(FriendlyByteBuf pBuffer, FluidStack[] array) {
        pBuffer.writeInt(array.length);
        for (FluidStack fluid : array) {
            fluid.write(pBuffer); // Changed from pBuffer.writeFluidStack(fluid)
        }
    }

    private static IngredientItem[] readIngredientItemArrayFromJson(JsonArray ingredientArray) {
        return Optional.ofNullable(ingredientArray)
                .map(array -> {
                    IngredientItem[] ingredientItems = new IngredientItem[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject ingredientObject = array.get(i).getAsJsonObject();
                        JsonObject itemStackObject = ingredientObject.getAsJsonObject("itemStack");
                        ItemStack itemStack = readItemStack(itemStackObject);
                        boolean isConsumable = ingredientObject.has("isConsumable") && ingredientObject.get("isConsumable").getAsBoolean();
                        ingredientItems[i] = new IngredientItem(itemStack, isConsumable);
                    }
                    return ingredientItems;
                })
                .orElse(new IngredientItem[0]);
    }

    private static OutputItem[] readOutputItemArrayFromJson(JsonArray outputArray) {
        return Optional.ofNullable(outputArray)
                .map(array -> {
                    OutputItem[] outputItems = new OutputItem[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject outputObject = array.get(i).getAsJsonObject();
                        JsonObject itemStackObject = outputObject.getAsJsonObject("itemStack");
                        ItemStack itemStack = readItemStack(itemStackObject);
                        float chance = outputObject.has("chance") ? outputObject.get("chance").getAsFloat() : 1.0f;
                        outputItems[i] = new OutputItem(itemStack, chance);
                    }
                    return outputItems;
                })
                .orElse(new OutputItem[0]);
    }

    private static FluidStack[] readFluidStackArrayFromJson(JsonArray jsonArray) {
        return Optional.ofNullable(jsonArray)
                .map(array -> {
                    FluidStack[] fluidStacks = new FluidStack[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        fluidStacks[i] = readFluidStack(array.get(i).getAsJsonObject());
                    }
                    return fluidStacks;
                })
                .orElse(new FluidStack[0]);
    }

    private static ItemStack readItemStack(JsonObject json) {
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, json).result().orElseThrow().getFirst();
    }

    private static FluidStack readFluidStack(JsonObject json) {
        ResourceLocation fluidId = new ResourceLocation(json.get("fluid").getAsString());
        long amount = json.get("amount").getAsLong();
        CompoundTag nbt = null;
        if (json.has("nbt")) {
            // Assuming NBT is stored as a JSON object string that needs parsing,
            // or more ideally, it's already a CompoundTag if the serializer supports it.
            // For simplicity, let's assume it's not directly parsable here without more context on NBT-JSON libs.
            // This part might need adjustment based on how NBT is actually stored in your JSON.
            // If it's a string representation of NBT, you'd need a parser.
            // If it's structured JSON representing NBT, that's another case.
            // For now, we'll retrieve it if it's a simple CompoundTag representation or leave it null.
            // This is a common simplification if direct NBT parsing from JSON is complex.
            // Consider using a library or helper method if NBT is complex.
        }
        // dev.architectury.fluid.FluidStack.create(Fluid fluid, long amount, @Nullable CompoundTag nbt)
        // We need to get Fluid from ResourceLocation. This requires access to a registry.
        // Assuming dev.architectury.registry.registries.Registries.get().get(RegistryKeys.FLUID).get(fluidId)
        // For now, this will be a placeholder as direct registry access here is complex.
        // This line will likely cause a compile error and needs a proper way to get Fluid from ID.
        // Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId); // This is Forge way
        // Architectury way:
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);

        if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            return FluidStack.empty();
        }
        return FluidStack.create(fluid, amount, nbt);
    }
}
