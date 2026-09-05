package com.nstut.nstutlib.items;

import com.nstut.nstutlib.NsTutLib;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class ItemRegistries {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NsTutLib.MOD_ID);

    public static final Supplier<Item> SMART_HAMMER = ITEMS.registerItem("smart_hammer", SmartHammer::new, properties -> properties.stacksTo(1));
    public static final Supplier<Item> STRUCTURE_SCANNER = ITEMS.registerItem("structure_scanner", StructureScanner::new, properties -> properties.stacksTo(1));

    public static final Set<Supplier<Item>> ITEM_SET = new HashSet<>() {{
        add(SMART_HAMMER);
    }};

    private ItemRegistries() {
    }
}
