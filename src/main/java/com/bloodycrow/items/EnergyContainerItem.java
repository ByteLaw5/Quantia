package com.bloodycrow.items;

import com.bloodycrow.Quantia;
import com.bloodycrow.util.CustomEnergyStorage;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EnergyContainerItem extends Item {
    private final LazyOptional<CustomEnergyStorage> energy = LazyOptional.of(this::createEnergyHandler);

    public EnergyContainerItem(Properties properties) {
        super(properties);
        ItemModelsProperties.registerProperty(this, new ResourceLocation(Quantia.MOD_ID, "energy"), (stack, world, living) -> {
            if(stack.getItem() instanceof EnergyContainerItem) {
                final float[] res = new float[1];
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energyHandler -> res[0] = (float)(energyHandler.getEnergyStored() / 1000));
                return res[0];
            }
            return 0F;
        });
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if(this.getClass() == EnergyContainerItem.class)
            return new ICapabilityProvider() {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    return getCapability(cap);
                }

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
                    if(cap == CapabilityEnergy.ENERGY) {
                        return energy.cast();
                    }
                    return LazyOptional.empty();
                }
            };
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public boolean updateItemStackNBT(CompoundNBT nbt) {
        energy.ifPresent(energyHandler -> energyHandler.deserializeNBT(nbt.getCompound("Energy")));
        return true;
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        final CompoundNBT nbt = new CompoundNBT();
        energy.ifPresent(energyHandler -> nbt.put("Energy", energyHandler.serializeNBT()));
        return nbt;
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(10000) {
            @Override
            public boolean canExtract() {
                return getEnergyStored() > 0;
            }

            @Override
            public boolean canReceive() {
                return getEnergyStored() < getMaxEnergyStored();
            }
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        energy.ifPresent(energyHandler -> tooltip.add(new TranslationTextComponent("quantia.energy_container.energy", energyHandler.getEnergyStored())));
    }
}
