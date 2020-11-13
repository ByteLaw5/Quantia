package com.bloodycrow.networking;

import com.bloodycrow.screens.RaidOverScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

//Meant to send from server to client.
public class RaidOverMessage extends Message {
    private final ItemStack reward;
    private final boolean won;

    public RaidOverMessage(PacketBuffer buf) {
        reward = buf.readItemStack();
        won = buf.readBoolean();
    }

    public RaidOverMessage(ItemStack stack, boolean won) {
        this.reward = stack;
        this.won = won;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeItemStack(reward);
        buf.writeBoolean(won);
    }

    @Override
    public void handleMessage(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getSender() == null) //If we're on the client.
                RaidOverScreen.open(reward, won);
        });
        ctx.get().setPacketHandled(true);
    }
}
