package com.bloodycrow.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;

public class DummyContainer extends Container {
    public DummyContainer() {
        super(null, 0);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {}
}
