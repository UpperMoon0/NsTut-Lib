package com.nstut.nstutlib.items;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.world.item.Item;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;

import java.util.HashSet;
import java.util.Set;

public class ItemRegistries {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(NsTutLib.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> SMART_HAMMER = ITEMS.register("smart_hammer", () -> new SmartHammer(new Item.Properties().stacksTo(1)));
    public static final RegistrySupplier<Item> STRUCTURE_SCANNER = ITEMS.register("structure_scanner", () -> new StructureScanner(new Item.Properties().stacksTo(1)));

    public static final Set<RegistrySupplier<Item>> ITEM_SET = new HashSet<>() {{
        add(SMART_HAMMER);
        add(STRUCTURE_SCANNER);
    }};
}