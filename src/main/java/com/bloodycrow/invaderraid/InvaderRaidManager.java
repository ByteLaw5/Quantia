package com.bloodycrow.invaderraid;

import com.bloodycrow.Quantia;
import com.bloodycrow.entity.AbstractInvaderRaiderEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;

public class InvaderRaidManager extends WorldSavedData {
    private final Queue<InvaderRaid> toAddQueue = Queues.newLinkedBlockingQueue(); //Exists to prevent ConcurrentModificationException's.
    private final Queue<Integer> toRemoveQueue = Queues.newLinkedBlockingQueue(); //Exists to prevent ConcurrentModificationException's.
    private final Map<Integer, InvaderRaid> byId = Maps.newHashMap();
    private final ServerWorld world;
    private int nextAvailableId;
    private int tick;

    private InvaderRaidManager(ServerWorld world) {
        super(getName(world));
        this.world = world;
    }

    public InvaderRaid get(int id) {
        return byId.get(id);
    }

    public void tick() {
        if(tick < Integer.MAX_VALUE)
            ++this.tick;
        else
            tick = 0;
        InvaderRaid next;
        while((next = toAddQueue.poll()) != null)
            byId.put(next.getId(), next);
        for(InvaderRaid raid : byId.values()) {
            if(!Quantia.Config.ENABLE_RAIDS.get())
                raid.stop();

            if(raid.isStopped()) {
                toRemoveQueue.offer(raid.getId());
                markDirty();
            } else
                raid.tick();
        }
        Integer remove;
        while((remove = toRemoveQueue.poll()) != null)
            byId.remove(remove);

        if(tick % 200 == 0)
            markDirty();
        if(Quantia.Config.ENABLE_RAIDS.get()) {
            if(tick % Quantia.Config.RAID_DELAY.get() == 0) {
                ServerPlayerEntity random = world.getRandomPlayer();
                if(random == null)
                    return;
                InvaderRaid raid = findOrCreateRaid(world, random.getPosition());
                if(!raid.isStarted())
                    toAddQueue.offer(raid);
                markDirty();
            }
        }
    }

    public static boolean canJoinRaid(AbstractInvaderRaiderEntity entity, InvaderRaid raid) {
        return (entity != null && raid != null && raid.getWorld() != null) && entity.isAlive() && entity.canJoinRaid() && entity.getIdleTime() <= 2400 && entity.world.getDimensionType() == raid.getWorld().getDimensionType();
    }

    @Override
    public void read(CompoundNBT nbt) {
        nextAvailableId = nbt.getInt("NextAvailableID");
        tick = nbt.getInt("Tick");
        ListNBT listnbt = nbt.getList("Raids", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < listnbt.size(); i++) {
            CompoundNBT compound = listnbt.getCompound(i);
            InvaderRaid raid = new InvaderRaid(world, compound);
            byId.put(raid.getId(), raid);
        }

        listnbt = nbt.getList("ToAddQueue", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < listnbt.size(); i++) {
            CompoundNBT compound = listnbt.getCompound(i);
            InvaderRaid raid = new InvaderRaid(compound.getInt("Id"), world, NBTUtil.readBlockPos(compound));
            this.toAddQueue.offer(raid);
        }

        listnbt = nbt.getList("ToRemoveQueue", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < listnbt.size(); i++) {
            CompoundNBT compound = listnbt.getCompound(i);
            this.toRemoveQueue.offer(compound.getInt("Id"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("NextAvailableID", nextAvailableId);
        compound.putInt("Tick", tick);
        ListNBT listnbt = new ListNBT();

        for(InvaderRaid raid : byId.values()) {
            CompoundNBT compoundNBT = new CompoundNBT();
            raid.write(compoundNBT);
            listnbt.add(compoundNBT);
        }

        ListNBT toAddQueue = new ListNBT();
        for(InvaderRaid invaderRaid : this.toAddQueue) {
            CompoundNBT nbt = new CompoundNBT();
            compound.putInt("Id", invaderRaid.getId());
            NBTUtil.writeBlockPos(invaderRaid.getCenter());
            toAddQueue.add(nbt);
        }

        ListNBT toRemoveQueue = new ListNBT();
        for(int invaderRaid : this.toRemoveQueue) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Id", invaderRaid);
        }

        compound.put("Raids", listnbt);
        compound.put("ToAddQueue", toAddQueue);
        compound.put("ToRemoveQueue", toRemoveQueue);
        return compound;
    }

    private InvaderRaid findOrCreateRaid(ServerWorld world, BlockPos pos) {
        InvaderRaid raid = getForWorld(world).findRaid(pos);
        return raid != null ? raid : new InvaderRaid(incrementNextId(), world, pos);
    }

    private int incrementNextId() {
        return ++nextAvailableId;
    }

    @Nullable
    public InvaderRaid findRaid(BlockPos pos) {
        return findRaid(pos, 9216);
    }

    @Nullable
    public InvaderRaid findRaid(BlockPos pos, int distance) {
        InvaderRaid raid = null;
        double d0 = distance;

        for(InvaderRaid raid1 : byId.values()) {
            double d = raid1.getCenter().distanceSq(pos);
            if(raid1.isActive() && d < d0) {
                raid = raid1;
                d0 = d;
            }
        }

        return raid;
    }

    @SuppressWarnings("deprecation")
    public static String getName(ServerWorld world) {
        return "invaderraids" + world.getDimensionType().getSuffix();
    }

    @Nonnull
    public static InvaderRaidManager getForWorld(@Nonnull ServerWorld world) {
        return world.getSavedData().getOrCreate(() -> new InvaderRaidManager(world), getName(world));
    }

    public static InvaderRaid createRaid(ServerWorld world, BlockPos pos) {
        InvaderRaidManager manager = getForWorld(world);
        InvaderRaid raid = new InvaderRaid(manager.incrementNextId(), world, pos);
        manager.toAddQueue.offer(raid);
        manager.markDirty();
        return raid;
    }
}