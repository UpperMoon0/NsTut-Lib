package com.nstut.nstutlib.creative_tabs;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.items.ItemRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabRegistries {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NsTutLib.MOD_ID);
    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> NSTUT_LIB_TAB = CREATIVE_MODE_TABS.register("nstutlib", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistries.SMART_HAMMER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (RegistryObject<Item> i : ItemRegistries.ITEM_SET) {
                    output.accept(i.get());
                }
                if (NsTutLib.IS_DEV_ENV) {
                    output.accept(ItemRegistries.STRUCTURE_SCANNER.get());
                }
            })
            .title(Component.translatable("itemGroup.nstutlib"))
            .build());
}
