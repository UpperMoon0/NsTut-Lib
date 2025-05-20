package com.nstut.nstutlib.util;

public enum EnergyTier {
    TIER_1(512, 512 * 1200),
    TIER_2(2048, 2048 * 1200),
    TIER_3(8192, 8192 * 1200),
    TIER_4(32768, 32768 * 1200);

    private final int transferRate; // FE per tick
    private final int capacity;     // FE

    EnergyTier(int transferRate, int capacity) {
        this.transferRate = transferRate;
        this.capacity = capacity;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public int getCapacity() {
        return capacity;
    }
}
