package com.randomteam.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage implements INBTSerializable<CompoundNBT> {
    public CustomEnergyStorage(int capacity) {
        super(capacity);
        this.maxExtract = 0;
        this.maxReceive = 0;
    }

    public CustomEnergyStorage(int capacity, int maxExtract) {
        super(capacity, 0, maxExtract);
    }

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void addEnergy(int energy) {
        this.energy += energy;
        if(this.energy > getMaxEnergyStored())
            this.energy = getEnergyStored();
    }

    protected void onEnergyChanged() {}

    public void consumeEnergy(int energy) {
        this.energy -= energy;
        if(this.energy < 0)
            this.energy = 0;
        onEnergyChanged();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("energy", getEnergyStored());
        return tag;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return super.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        setEnergy(nbt.getInt("energy"));
    }
}
