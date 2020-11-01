package com.randomteam.list;

import com.randomteam.containers.ArcaneCrafterContainer;
import com.randomteam.containers.EnergyProducerContainer;
import com.randomteam.containers.EnergyReceiverContainer;

import net.minecraft.inventory.container.ContainerType;

public class ContainerList {
    public static ContainerType<EnergyProducerContainer> energy_producer;
    public static ContainerType<EnergyReceiverContainer> energy_receiver;
    public static ContainerType<ArcaneCrafterContainer> arcane_crafter;
}
