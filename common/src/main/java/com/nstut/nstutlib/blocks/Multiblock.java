package com.nstut.nstutlib.blocks;

import com.nstut.nstutlib.models.MultiblockPattern;

public interface Multiblock {
    MultiblockPattern getMultiblockPattern();
    int getControllerSouthOffsetX();
    int getControllerSouthOffsetY();
    int getControllerSouthOffsetZ();
}
