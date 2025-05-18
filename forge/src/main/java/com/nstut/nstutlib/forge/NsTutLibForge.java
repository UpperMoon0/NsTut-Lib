package com.nstut.nstutlib.forge;

import com.nstut.nstutlib.NsTutLib;
import net.minecraftforge.fml.common.Mod;

@Mod(NsTutLib.MOD_ID)
public class NsTutLibForge {
    public NsTutLibForge() {
        NsTutLib.init();
    }
}
