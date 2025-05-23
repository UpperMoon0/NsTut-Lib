package com.nstut.nstutlib.core.registry;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.menu.HatchMenu;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import dev.architectury.registry.menu.MenuRegistry;

public class NsTutLibMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(NsTutLib.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<HatchMenu>> HATCH_MENU =
            MENU_TYPES.register(new ResourceLocation(NsTutLib.MOD_ID, "hatch_menu"), () -> MenuRegistry.ofExtended(HatchMenu::new));

    public static void register() {
        MENU_TYPES.register();
    }
}
