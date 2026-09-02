package com.nstut.nstutlib.items;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;

public class ItemRegistries {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NsTutLib.MOD_ID);

    public static final RegistryObject<Item> SMART_HAMMER = ITEMS.register("smart_hammer", () -> new SmartHammer(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STRUCTURE_SCANNER = ITEMS.register("structure_scanner", () -> new StructureScanner(new Item.Properties().stacksTo(1)));;

    public static final Set<RegistryObject<Item>> ITEM_SET = new HashSet<>() {{
        add(SMART_HAMMER);
    }};
}