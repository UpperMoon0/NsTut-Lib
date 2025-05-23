package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.menu.HatchMenu;
import com.nstut.nstutlib.menu.FluidHatchMenu;
import com.nstut.nstutlib.menu.ItemHatchMenu;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import dev.architectury.registry.menu.MenuRegistry;

public class NsTutLibMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(NsTutLib.MOD_ID, Registries.MENU);

    @Deprecated // This generic menu type should ideally be replaced by specific ones like ITEM_HATCH_MENU or FLUID_HATCH_MENU
    public static final RegistrySupplier<MenuType<HatchMenu>> HATCH_MENU =
            MENU_TYPES.register(new ResourceLocation(NsTutLib.MOD_ID, "hatch_menu"), () -> MenuRegistry.ofExtended(HatchMenu::new));

    public static final RegistrySupplier<MenuType<ItemHatchMenu>> ITEM_HATCH_MENU =
            MENU_TYPES.register(new ResourceLocation(NsTutLib.MOD_ID, "item_hatch_menu"), () -> MenuRegistry.ofExtended(ItemHatchMenu::new));

    public static final RegistrySupplier<MenuType<FluidHatchMenu>> FLUID_HATCH_MENU =
            MENU_TYPES.register(new ResourceLocation(NsTutLib.MOD_ID, "fluid_hatch_menu"), () -> MenuRegistry.ofExtended(FluidHatchMenu::new));

    public static void register() {
        MENU_TYPES.register();
    }
}
