package com.nstut.nstutlib.client;

import com.nstut.nstutlib.views.StructureScannerScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void openStructureScanner(int firstX,
                                            int firstY,
                                            int firstZ,
                                            int secondX,
                                            int secondY,
                                            int secondZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.setScreen(new StructureScannerScreen(
                minecraft.level,
                firstX, firstY, firstZ,
                secondX, secondY, secondZ));
    }
}
