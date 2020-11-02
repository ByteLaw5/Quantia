package com.bloodycrow.tileentities;

import com.bloodycrow.containers.EnergyProducerContainer;
import com.bloodycrow.list.TileEntityList;
import com.bloodycrow.util.CustomEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyProducerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private final LazyOptional<CustomEnergyStorage> energy = LazyOptional.of(this::createEnergyHandler);
    private final LazyOptional<ItemStackHandler> items = LazyOptional.of(this::createItemHandler);
    private ITextComponent customName;

    public EnergyProducerTileEntity() {
        super(TileEntityList.energy_producer);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        items.ifPresent(h -> h.deserializeNBT(nbt.getCompound("Items")));
        energy.ifPresent(h -> h.deserializeNBT(nbt.getCompound("Energy")));
        if(nbt.contains("CustomName", Constants.NBT.TAG_STRING))
            setCustomName(ITextComponent.Serializer.getComponentFromJson(nbt.getString("CustomName")));
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        items.ifPresent(h -> compound.put("Items", h.serializeNBT()));
        energy.ifPresent(h -> compound.put("Energy", h.serializeNBT()));
        if(customName != null)
            compound.putString("CustomName", ITextComponent.Serializer.toJson(customName));
        return super.write(compound);
    }

    @Override
    public void tick() {
        if(world.isRemote)
            return;

        items.ifPresent(h -> energy.ifPresent(energy -> {
            ItemStack stack = h.getStackInSlot(0);
            if(!stack.isEmpty()) {
                h.extractItem(0, 1, false);
                energy.addEnergy(50);
                markDirty();
            }
        }));

        sendOutPower();
    }

    private void sendOutPower() {
        energy.ifPresent(energyHandler -> {
            if(energyHandler.canExtract() && hasWorld() && !isRemoved()) {
                for(Direction direction : Direction.values()) {
                    TileEntity te = this.world.getTileEntity(this.getPos().offset(direction));
                    if(te != null) {
                        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(otherEnergyHandler -> {
                            if(otherEnergyHandler.canReceive() && otherEnergyHandler instanceof CustomEnergyStorage) {
                                int transferredEnergy = Math.min(100, energyHandler.getEnergyStored());
                                if(otherEnergyHandler.getEnergyStored() + transferredEnergy > otherEnergyHandler.getMaxEnergyStored()) {
                                    transferredEnergy = Math.min(transferredEnergy, otherEnergyHandler.getMaxEnergyStored() - otherEnergyHandler.getEnergyStored());
                                }
                                energyHandler.consumeEnergy(transferredEnergy);
                                ((CustomEnergyStorage)otherEnergyHandler).addEnergy(transferredEnergy);
                                markDirty();
                            }
                        });
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(10000, 20) {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }

            @Override
            public boolean canExtract() {
                return getEnergyStored() > 0;
            }

            @Override
            public boolean canReceive() {
                return false;
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

    @Override
    public void remove() {
        super.remove();
        energy.invalidate();
        items.invalidate();
    }
}
