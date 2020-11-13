package com.bloodycrow.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class Message {
    public abstract void encode(PacketBuffer buf);
    public abstract void handleMessage(Supplier<NetworkEvent.Context> ctx);
}
