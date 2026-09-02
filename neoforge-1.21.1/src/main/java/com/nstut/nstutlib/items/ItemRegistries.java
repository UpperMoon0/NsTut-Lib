package com.nstut.nstutlib.items;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class ItemRegistries {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NsTutLib.MOD_ID);

    public static final Supplier<Item> SMART_HAMMER = ITEMS.register("smart_hammer", () -> new SmartHammer(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> STRUCTURE_SCANNER = ITEMS.register("structure_scanner", () -> new StructureScanner(new Item.Properties().stacksTo(1)));

    public static final Set<Supplier<Item>> ITEM_SET = new HashSet<>() {{
        add(SMART_HAMMER);
    }};

    private ItemRegistries() {
    }
}
