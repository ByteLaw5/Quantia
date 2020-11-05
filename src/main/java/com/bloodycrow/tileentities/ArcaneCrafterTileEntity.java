package com.bloodycrow.tileentities;

import com.bloodycrow.containers.ArcaneCrafterContainer;
import com.bloodycrow.list.TileEntityList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tile entity for Arcane Crafter.
 */
public class ArcaneCrafterTileEntity extends TileEntity implements INamedContainerProvider {
    private final LazyOptional<ItemStackHandler> items = LazyOptional.of(this::createItemHandler);
    private ITextComponent customName;

    public ArcaneCrafterTileEntity() {
        super(TileEntityList.arcane_crafter);
        customName = null;
    }
    /**
     * Gets a display name for the entity.
     * @return Tile entity display name
     */
    @Override
    public ITextComponent getDisplayName() {
        return customName == null ? new TranslationTextComponent("quantia.arcane_crafter.name") : customName;
    }

    /**
     * Gets tile entity capabilities
     * @param cap Capability
     * @param <T> Type of the capability
     * @return Optional capability.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if(!this.removed) {
            if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return items.cast();
            }
        }
        return super.getCapability(cap);
    }

    /**
     * Creates item handler
     * @return Item handler.
     */
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(26) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }
    /**
     * Sets a custom name for this tile entity.
     * @param component Tile entity name.
     */
    public void setCustomName(ITextComponent component) {
        this.customName = component;
    }
    /**
     * Creates menu for the tile entity.
     * @param windowId The window id
     * @param playerInventory Inventory of player entity
     * @param player Inventory owner
     * @return New container
     */
    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ArcaneCrafterContainer(windowId, world, pos, playerInventory, IWorldPosCallable.of(world, pos));
    }
}
