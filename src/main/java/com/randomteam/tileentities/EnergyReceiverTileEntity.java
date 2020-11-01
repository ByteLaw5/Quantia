package com.randomteam.tileentities;

import com.randomteam.containers.EnergyReceiverContainer;
import com.randomteam.list.TileEntityList;
import com.randomteam.util.CustomEnergyStorage;
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

public class EnergyReceiverTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private final LazyOptional<CustomEnergyStorage> energy = LazyOptional.of(this::createEnergyHandler);
    private final LazyOptional<ItemStackHandler> items = LazyOptional.of(this::createItemHandler);
    private ITextComponent customName;

    public EnergyReceiverTileEntity() {
        super(TileEntityList.energy_receiver);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        energy.ifPresent(h -> h.deserializeNBT(nbt.getCompound("Energy")));
        if(nbt.contains("CustomName", Constants.NBT.TAG_STRING))
            setCustomName(ITextComponent.Serializer.getComponentFromJson(nbt.getString("CustomName")));
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        energy.ifPresent(h -> compound.put("Energy", h.serializeNBT()));
        if(customName != null)
            compound.putString("CustomName", ITextComponent.Serializer.toJson(customName));
        return super.write(compound);
    }

    @Override
    public void tick() {
        energy.ifPresent(energyHandler -> items.ifPresent(itemHandler -> {
            ItemStack stack = itemHandler.getStackInSlot(0);
            if(stack != ItemStack.EMPTY && hasWorld() && !isRemoved()) {
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(otherEnergyHandler -> {
                    if(energyHandler.canExtract() && otherEnergyHandler.canReceive() && otherEnergyHandler instanceof CustomEnergyStorage) {
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
        }));
    }

    public void setCustomName(ITextComponent component) {
        this.customName = component;
    }

    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
            }
        };
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(10000, 0) {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }

            @Override
            public boolean canReceive() {
                return getEnergyStored() < getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return getEnergyStored() > 0;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if(!this.removed) {
            if(cap == CapabilityEnergy.ENERGY)
                return energy.cast();
            if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                return items.cast();
        }
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public ITextComponent getDisplayName() {
        return customName == null ? new TranslationTextComponent("quantia.energy_receiver.name") : customName;
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
        return new EnergyReceiverContainer(p_createMenu_1_, p_createMenu_3_.getEntityWorld(), this.getPos(), p_createMenu_2_, p_createMenu_3_);
    }

    @Override
    public void remove() {
        super.remove();
        energy.invalidate();
        items.invalidate();
    }
}
