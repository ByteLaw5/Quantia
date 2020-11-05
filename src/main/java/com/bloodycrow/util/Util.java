package com.bloodycrow.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class Util {
    public static ItemStack[] toArray(IItemHandler handler) {
        return net.minecraft.util.Util.make(new ItemStack[handler.getSlots()], array -> {
            for(int i = 0; i < handler.getSlots(); i++)
                array[i] = handler.getStackInSlot(i);
        });
    }
}
