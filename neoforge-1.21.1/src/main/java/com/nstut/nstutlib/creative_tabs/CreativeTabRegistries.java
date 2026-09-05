package com.nstut.nstutlib.creative_tabs;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.items.ItemRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CreativeTabRegistries {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NsTutLib.MOD_ID);

    @SuppressWarnings("unused")
    public static final Supplier<CreativeModeTab> NSTUT_LIB_TAB = CREATIVE_MODE_TABS.register("nstutlib", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistries.SMART_HAMMER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (Supplier<Item> item : ItemRegistries.ITEM_SET) {
                    output.accept(item.get());
                }
                if (NsTutLib.IS_DEV_ENV) {
                    output.accept(ItemRegistries.STRUCTURE_SCANNER.get());
                }
            })
            .title(Component.translatable("itemGroup.nstutlib"))
            .build());

    private CreativeTabRegistries() {
    }
}
