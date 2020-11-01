package com.randomteam.items;

import com.randomteam.util.CustomEnergyStorage;
import com.randomteam.util.EnergyWrapper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyContainerItem extends Item {
    private final NonNullSupplier<CustomEnergyStorage> energy = this::createEnergyHandler;

    public EnergyContainerItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if(this.getClass() == EnergyContainerItem.class)
            return new EnergyWrapper(energy);
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public boolean updateItemStackNBT(CompoundNBT nbt) {
        energy.get().deserializeNBT(nbt);
        return true;
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return energy.get().serializeNBT();
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(10000);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("quantia.energy_container.energy", energy.get().getEnergyStored()));
    }
}
