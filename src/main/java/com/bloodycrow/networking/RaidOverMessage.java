package com.bloodycrow.networking;

import com.bloodycrow.screens.RaidOverScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

//Meant to send from server to client.
public class RaidOverMessage {
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

    public void toBytes(PacketBuffer buf) {
        buf.writeItemStack(reward);
        buf.writeBoolean(won);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Minecraft.getInstance().displayGuiScreen(new RaidOverScreen(reward, won)));
        ctx.get().setPacketHandled(true);
    }
}
