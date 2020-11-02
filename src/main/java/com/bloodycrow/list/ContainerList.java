package com.bloodycrow.list;

import com.bloodycrow.containers.ArcaneCrafterContainer;
import com.bloodycrow.containers.EnergyProducerContainer;
import com.bloodycrow.containers.EnergyReceiverContainer;

import net.minecraft.inventory.container.ContainerType;

public class ContainerList {
    public static ContainerType<EnergyProducerContainer> energy_producer;
    public static ContainerType<EnergyReceiverContainer> energy_receiver;
    public static ContainerType<ArcaneCrafterContainer> arcane_crafter;
}
