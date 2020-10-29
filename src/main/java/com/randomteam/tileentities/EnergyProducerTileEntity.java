package com.randomteam.tileentities;

import com.randomteam.containers.EnergyProducerContainer;
import com.randomteam.list.TileEntityList;
import com.randomteam.util.CustomEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class EnergyProducerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private final LazyOptional<CustomEnergyStorage> energy = LazyOptional.of(this::createEnergyHandler);
    private final LazyOptional<ItemStackHandler> items = LazyOptional.of(this::createItemHandler);
    private int counter;
    private ITextComponent customName;

    public EnergyProducerTileEntity() {
        super(TileEntityList.energy_producer);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        items.ifPresent(h -> h.deserializeNBT(nbt.getCompound("Items")));
        energy.ifPresent(h -> h.deserializeNBT(nbt.getCompound("Energy")));
        counter = nbt.getInt("Counter");
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        items.ifPresent(h -> compound.put("Items", h.serializeNBT()));
        energy.ifPresent(h -> compound.put("Energy", h.serializeNBT()));
        compound.putInt("Counter", counter);
        return super.write(compound);
    }

    @Override
    public void tick() {
        if(world.isRemote)
            return;

        if(counter > 0) {
            counter--;
            if(counter <= 0)
                energy.ifPresent(e -> e.addEnergy(100));
            markDirty();
        } else {
            items.ifPresent(h -> {
                ItemStack stack = h.getStackInSlot(0);
                if(stack.getItem() == Items.DIAMOND) {
                    h.extractItem(0, 1, false);
                    counter = 20;
                    markDirty();
                }
            });
        }

        sendOutPower();
    }

    private void sendOutPower() {
        energy.ifPresent(energy -> {
            AtomicInteger capacity = new AtomicInteger(energy.getEnergyStored());
            if(capacity.get() > 0) {
                for(Direction direction : Direction.values()) {
                    TileEntity te = world.getTileEntity(pos.offset(direction));
                    if(te != null) {
                        boolean doContinue = te.getCapability(CapabilityEnergy.ENERGY, direction).map(handler -> {
                            if(handler.canReceive()) {
                                int received = handler.receiveEnergy(Math.min(capacity.get(), 100), false);
                                capacity.addAndGet(-received);
                                energy.consumeEnergy(received);
                                markDirty();
                                return capacity.get() > 0;
                            } else {
                                return true;
                            }
                        }).orElse(true);
                        if(!doContinue)
                            return;
                    }
                }
            }
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if(!this.removed) {
            if (cap == CapabilityEnergy.ENERGY) {
                return energy.cast();
            } else if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return items.cast();
            }
        }
        return super.getCapability(cap);
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(10000, 0) {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }
        };
    }

    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }

    @Override
    public ITextComponent getDisplayName() {
        return customName == null ? new TranslationTextComponent("quantia.energy_producer.name") : customName;
    }

    public void setCustomName(ITextComponent component) {
        this.customName = component;
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        return new EnergyProducerContainer(windowId, player.getEntityWorld(), this.getPos(), playerInventory, player);
    }
}
