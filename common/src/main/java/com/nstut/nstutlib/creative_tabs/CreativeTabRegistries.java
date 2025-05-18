package com.nstut.nstutlib.creative_tabs;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.items.ItemRegistries;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class CreativeTabRegistries {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(NsTutLib.MOD_ID, Registries.CREATIVE_MODE_TAB);
    @SuppressWarnings("unused")
    public static final RegistrySupplier<CreativeModeTab> NSTUT_LIB_TAB = CREATIVE_MODE_TABS.register("nstutlib", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0) // Added Row.TOP and 0
            // .withTabsBefore(CreativeModeTabs.COMBAT) // Commented out for now
            .icon(() -> ItemRegistries.SMART_HAMMER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (RegistrySupplier<Item> i : ItemRegistries.ITEM_SET) {
                    output.accept(i.get());
                }
                if (NsTutLib.IS_DEV_ENV) {
                    // Add dev-only items here if any
                }
            })
            .title(Component.translatable("itemGroup.nstutlib"))
            .build());

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}
