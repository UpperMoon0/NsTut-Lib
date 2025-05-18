package com.nstut.nstutlib.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import dev.architectury.fluid.FluidStack; 
import com.nstut.nstutlib.recipes.IngredientFluid;
import com.nstut.nstutlib.recipes.OutputFluid;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RecipeSerializerFactory<R extends ModRecipe<D>, D extends ModRecipeData> {

    private final RecipeFactory<R, D> factory;

    private RecipeSerializerFactory(RecipeFactory<R, D> factory) {
        this.factory = factory;
    }

    public static <R extends ModRecipe<D>, D extends ModRecipeData> RecipeSerializer<R> create(RecipeType<R> type, RecipeFactory<R, D> factory) {
        RecipeSerializerFactory<R, D> recipeSerializerFactory = new RecipeSerializerFactory<>(factory);
        return recipeSerializerFactory.createSerializer();
    }

    private RecipeSerializer<R> createSerializer() {
        return new RecipeSerializer<R>() {
            @Override
            public R fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
                ModRecipeData recipeData = readRecipeDataFromJson(pSerializedRecipe);
                // Unchecked cast, but factory is typed to produce R with D, and ModRecipeData is D
                return factory.create(pRecipeId, (D) recipeData);
            }

            @Override
            public R fromNetwork(@NotNull ResourceLocation pRecipeId, @NotNull FriendlyByteBuf pBuffer) {
                IngredientItem[] itemIngredients = readItemIngredientArray(pBuffer);
                OutputItem[] itemResults = readOutputItemArray(pBuffer);
                IngredientFluid[] fluidIngredients = readIngredientFluidArray(pBuffer);
                OutputFluid[] fluidResults = readOutputFluidArray(pBuffer);
                int totalEnergy = pBuffer.readInt();

                // Construct D directly if possible, or ensure ModRecipeData can be cast
                D recipeData = (D) new ModRecipeData(itemIngredients, itemResults, fluidIngredients, fluidResults, totalEnergy);
                return factory.create(pRecipeId, recipeData);
            }

            @Override
            public void toNetwork(@NotNull FriendlyByteBuf pBuffer, @NotNull R pRecipe) {
                ModRecipeData recipeContainer = pRecipe.getRecipeData();

                writeIngredientItemArray(pBuffer, recipeContainer.getIngredientItems());
                writeOutputItemArray(pBuffer, recipeContainer.getOutputItems());
                writeIngredientFluidArray(pBuffer, recipeContainer.getFluidIngredients());
                writeOutputFluidArray(pBuffer, recipeContainer.getFluidOutputs());

                pBuffer.writeInt(recipeContainer.getTotalEnergy());
            }

            @Override
            public JsonObject toJson(@NotNull R pRecipe) { // Ensure R is the correct type here
                JsonObject json = new JsonObject();
                ModRecipeData recipeContainer = pRecipe.getRecipeData();

                JsonArray itemInputsArray = new JsonArray();
                writeIngredientItemArrayToJson(itemInputsArray, recipeContainer.getIngredientItems());
                json.add("itemInputs", itemInputsArray);

                JsonArray itemOutputsArray = new JsonArray();
                writeOutputItemArrayToJson(itemOutputsArray, recipeContainer.getOutputItems());
                json.add("itemOutputs", itemOutputsArray);

                JsonArray fluidInputsArray = new JsonArray();
                writeIngredientFluidArrayToJson(fluidInputsArray, recipeContainer.getFluidIngredients());
                json.add("fluidInputs", fluidInputsArray);

                JsonArray fluidOutputsArray = new JsonArray();
                writeOutputFluidArrayToJson(fluidOutputsArray, recipeContainer.getFluidOutputs());
                json.add("fluidOutputs", fluidOutputsArray);

                json.addProperty("energy", recipeContainer.getTotalEnergy());
                return json;
            }
        };
    }

    private static ModRecipeData readRecipeDataFromJson(JsonObject pSerializedRecipe) {
        IngredientItem[] ingredientItems = readIngredientItemArrayFromJson(pSerializedRecipe.getAsJsonArray("itemInputs"));
        OutputItem[] outputItems = readOutputItemArrayFromJson(pSerializedRecipe.getAsJsonArray("itemOutputs"));
        IngredientFluid[] fluidIngredients = readIngredientFluidArrayFromJson(pSerializedRecipe.getAsJsonArray("fluidInputs"));
        OutputFluid[] fluidResults = readOutputFluidArrayFromJson(pSerializedRecipe.getAsJsonArray("fluidOutputs"));
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

    private static IngredientFluid[] readIngredientFluidArray(FriendlyByteBuf pBuffer) {
        int length = pBuffer.readInt();
        IngredientFluid[] array = new IngredientFluid[length];
        for (int i = 0; i < length; i++) {
            FluidStack fluidStack = FluidStack.read(pBuffer);
            boolean isConsumable = pBuffer.readBoolean();
            array[i] = new IngredientFluid(fluidStack, isConsumable);
        }
        return array;
    }

    private static void writeIngredientFluidArray(FriendlyByteBuf pBuffer, IngredientFluid[] array) {
        pBuffer.writeInt(array.length);
        for (IngredientFluid fluid : array) {
            fluid.getFluidStack().write(pBuffer);
            pBuffer.writeBoolean(fluid.isConsumable());
        }
    }

    private static OutputFluid[] readOutputFluidArray(FriendlyByteBuf pBuffer) {
        int length = pBuffer.readInt();
        OutputFluid[] array = new OutputFluid[length];
        for (int i = 0; i < length; i++) {
            FluidStack fluidStack = FluidStack.read(pBuffer);
            float chance = pBuffer.readFloat();
            array[i] = new OutputFluid(fluidStack, chance);
        }
        return array;
    }

    private static void writeOutputFluidArray(FriendlyByteBuf pBuffer, OutputFluid[] array) {
        pBuffer.writeInt(array.length);
        for (OutputFluid fluid : array) {
            fluid.getFluidStack().write(pBuffer);
            pBuffer.writeFloat(fluid.getChance());
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

    private static IngredientFluid[] readIngredientFluidArrayFromJson(JsonArray fluidArray) {
        return Optional.ofNullable(fluidArray)
                .map(array -> {
                    IngredientFluid[] ingredientFluids = new IngredientFluid[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject fluidObject = array.get(i).getAsJsonObject();
                        FluidStack fluidStack = readFluidStack(fluidObject.getAsJsonObject("fluidStack"));
                        boolean isConsumable = fluidObject.has("isConsumable") && fluidObject.get("isConsumable").getAsBoolean();
                        ingredientFluids[i] = new IngredientFluid(fluidStack, isConsumable);
                    }
                    return ingredientFluids;
                })
                .orElse(new IngredientFluid[0]);
    }

    private static OutputFluid[] readOutputFluidArrayFromJson(JsonArray fluidArray) {
        return Optional.ofNullable(fluidArray)
                .map(array -> {
                    OutputFluid[] outputFluids = new OutputFluid[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject fluidObject = array.get(i).getAsJsonObject();
                        FluidStack fluidStack = readFluidStack(fluidObject.getAsJsonObject("fluidStack"));
                        float chance = fluidObject.has("chance") ? fluidObject.get("chance").getAsFloat() : 1.0f;
                        outputFluids[i] = new OutputFluid(fluidStack, chance);
                    }
                    return outputFluids;
                })
                .orElse(new OutputFluid[0]);
    }

    private static void writeIngredientItemArray(FriendlyByteBuf pBuffer, IngredientItem[] array) {
        pBuffer.writeInt(array.length);
        for (IngredientItem item : array) {
            pBuffer.writeItem(item.getItemStack());
            pBuffer.writeBoolean(item.isConsumable()); // Added missing isConsumable
        }
    }

    private static void writeOutputItemArray(FriendlyByteBuf pBuffer, OutputItem[] array) {
        pBuffer.writeInt(array.length);
        for (OutputItem item : array) {
            pBuffer.writeItem(item.getItemStack());
            pBuffer.writeFloat(item.getChance());
        }
    }

    private static void writeIngredientItemArrayToJson(JsonArray jsonArray, IngredientItem[] items) {
        if (items == null) return;
        for (IngredientItem item : items) {
            JsonObject itemObject = new JsonObject();
            itemObject.add("itemStack", writeItemStack(item.getItemStack()));
            itemObject.addProperty("isConsumable", item.isConsumable());
            jsonArray.add(itemObject);
        }
    }

    private static void writeOutputItemArrayToJson(JsonArray jsonArray, OutputItem[] items) {
        if (items == null) return;
        for (OutputItem item : items) {
            JsonObject itemWrapper = new JsonObject(); // Wrapper to hold itemStack and chance
            itemWrapper.add("itemStack", writeItemStack(item.getItemStack()));
            itemWrapper.addProperty("chance", item.getChance());
            jsonArray.add(itemWrapper);
        }
    }

    private static void writeIngredientFluidArrayToJson(JsonArray jsonArray, IngredientFluid[] fluids) {
        if (fluids == null) return;
        for (IngredientFluid fluid : fluids) {
            JsonObject fluidObject = new JsonObject();
            fluidObject.add("fluidStack", writeFluidStack(fluid.getFluidStack()));
            fluidObject.addProperty("isConsumable", fluid.isConsumable());
            jsonArray.add(fluidObject);
        }
    }

    private static void writeOutputFluidArrayToJson(JsonArray jsonArray, OutputFluid[] fluids) {
        if (fluids == null) return;
        for (OutputFluid fluid : fluids) {
            JsonObject fluidObject = new JsonObject();
            fluidObject.add("fluidStack", writeFluidStack(fluid.getFluidStack()));
            fluidObject.addProperty("chance", fluid.getChance());
            jsonArray.add(fluidObject);
        }
    }

    private static ItemStack readItemStack(JsonObject json) {
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> {
                    throw new JsonParseException("Failed to deserialize ItemStack from JSON: " + json + " due to: " + error);
                })
                .map(com.mojang.datafixers.util.Pair::getFirst)
                .orElseThrow(() -> new JsonParseException("Failed to deserialize ItemStack from JSON (orElseThrow): " + json));
    }

    private static JsonElement writeItemStack(ItemStack stack) {
        return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack)
                .resultOrPartial(errorMessage -> {
                    throw new JsonParseException("Failed to serialize ItemStack to JSON: " + stack + " due to: " + errorMessage);
                })
                .orElseThrow(() -> new JsonParseException("Could not serialize ItemStack to JSON: " + stack));
    }

    private static FluidStack readFluidStack(JsonObject json) {
        ResourceLocation fluidId = new ResourceLocation(json.get("fluid").getAsString());
        long amount = json.get("amount").getAsLong();
        CompoundTag nbt = null;
        if (json.has("nbt")) {
            JsonElement nbtJson = json.get("nbt");
            nbt = CompoundTag.CODEC.parse(JsonOps.INSTANCE, nbtJson)
                    .resultOrPartial(errorMessage -> {
                        System.err.println("Failed to parse NBT for FluidStack: " + errorMessage);
                    })
                    .orElse(null);
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);

        if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            return FluidStack.empty();
        }
        return FluidStack.create(fluid, amount, nbt);
    }

    private static JsonObject writeFluidStack(FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
        json.addProperty("amount", stack.getAmount());
        if (stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                JsonElement nbtJson = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, nbt)
                        .resultOrPartial(errorMessage -> {
                            System.err.println("Failed to encode NBT for FluidStack: " + errorMessage);
                        })
                        .orElse(null);
                if (nbtJson != null && !nbtJson.isJsonNull()) {
                    json.add("nbt", nbtJson);
                }
            }
        }
        return json;
    }

    public interface RecipeFactory<R extends ModRecipe<D>, D extends ModRecipeData> {
        R create(ResourceLocation id, D recipeData);
    }
}
