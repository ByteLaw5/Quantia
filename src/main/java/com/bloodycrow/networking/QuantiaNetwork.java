package com.bloodycrow.networking;

import com.bloodycrow.Quantia;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class QuantiaNetwork {
    public static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Quantia.MOD_ID, "network_channel"), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(++id,
                RaidOverMessage.class,
                RaidOverMessage::toBytes,
                RaidOverMessage::new,
                RaidOverMessage::handle);
    }
}
