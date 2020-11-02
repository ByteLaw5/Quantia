package com.bloodycrow.list;

import com.bloodycrow.tileentities.ArcaneCrafterTileEntity;
import com.bloodycrow.tileentities.EnergyProducerTileEntity;
import com.bloodycrow.tileentities.EnergyReceiverTileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityList {
    public static TileEntityType<EnergyProducerTileEntity> energy_producer;
    public static TileEntityType<ArcaneCrafterTileEntity> arcane_crafter;
    public static TileEntityType<EnergyReceiverTileEntity> energy_receiver;
}
