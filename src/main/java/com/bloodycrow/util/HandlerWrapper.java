package com.bloodycrow.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class HandlerWrapper extends Inventory {
    private final IItemHandlerModifiable handler;

    public HandlerWrapper(IItemHandlerModifiable handler) {
        this.handler = handler;
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack res = super.decrStackSize(index, count);
        handler.setStackInSlot(index, res);
        return res;
    }
}
