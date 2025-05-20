package com.nstut.nstutlib.transfer;

public interface IEnergyStorage extends ITransferStorage {
    long receiveEnergy(long maxReceive, boolean simulate);
    long extractEnergy(long maxExtract, boolean simulate);
    long getEnergyStored();
    long getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
