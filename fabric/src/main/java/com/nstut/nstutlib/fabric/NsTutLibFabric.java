package com.nstut.nstutlib.fabric;

import com.nstut.nstutlib.NsTutLib;
import net.fabricmc.api.ModInitializer;

public class NsTutLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NsTutLib.init();
    }
}
