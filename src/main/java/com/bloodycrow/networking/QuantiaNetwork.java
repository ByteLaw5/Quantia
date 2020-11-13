package com.bloodycrow.networking;

import com.bloodycrow.Quantia;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class QuantiaNetwork {
    public static SimpleChannel INSTANCE;
    private static int id = 0;

    /**
     * Registers all of the custom "messages" to the channel so it can be used to send information between sides.
     */
    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Quantia.MOD_ID, "network_channel"), () -> "1.0", s -> true, s -> true);

        registerMessage(RaidOverMessage.class, RaidOverMessage::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <T extends Message> void registerMessage(Class<T> clazz, Function<PacketBuffer, T> decoder, @Nullable NetworkDirection direction) {
        INSTANCE.registerMessage(++id, clazz, T::encode, decoder, T::handleMessage, Optional.ofNullable(direction));
    }
}
