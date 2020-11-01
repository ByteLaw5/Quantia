package com.randomteam.containers;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

public class ArcaneCrafterContainer extends RecipeBookContainer<CraftingInventory> {
    private final CraftingInventory matrix;
    private final CraftResultInventory result;
    private final IWorldPosCallable callable;
    private final PlayerEntity player;

    public ArcaneCrafterContainer(int p_i50089_1_, PlayerInventory p_i50089_2_) {
        this(p_i50089_1_, p_i50089_2_, IWorldPosCallable.DUMMY);
    }

    public ArcaneCrafterContainer(int windowId, PlayerInventory inventory, IWorldPosCallable callable) {
        super(ContainerType.CRAFTING, windowId);
        matrix = new CraftingInventory(this, 3, 3);
        result = new CraftResultInventory();
        this.callable = callable;
        player = inventory.player;
        // Result of the crafting
        // Player inventory, container matrix, container result, slot index, slot x, slot y
        // Default X: 124
        addSlot(new CraftingResultSlot(inventory.player, matrix, result, 0, 124, 35));
        int i, j;
        // Draws 3x3 grid.
        for(i = 0; i < 3; i++) // 3 rows
            for(j = 0; j < 3; j++) // 3 columns
                addSlot(new Slot(matrix, j + i * 3, 30 + j * 18, 17 + i * 18));
        // Player's inventory
        for(i = 0; i < 3; ++i) // 3 rows
            for(j = 0; j < 9; j++) // 9 columns
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        // Player's hotbar
        for(i = 0; i < 9; i++) // 1 row, 9 columns
            addSlot(new Slot(inventory, i, 8 + i * 18, 142));
    }

    /**
     * Updates crafting result
     * @param index Slot index(?)
     * @param world World of the block which uses this container
     * @param player Player which activated this
     * @param craftingInventory This container's crafting inventory
     * @param resultInventory Inventory of the result
     */
    protected static void updateCraftingResult(int index, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftResultInventory resultInventory) {
        if (!world.isRemote) {
            // Gets player as server player entity
            ServerPlayerEntity serverEntity = (ServerPlayerEntity)player;
            // Creates empty stack
            ItemStack stack = ItemStack.EMPTY;
            // Gets either null or existing crafting recipe
            Optional<ICraftingRecipe> optionalRecipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, world);
            // If it's not null
            if (optionalRecipe.isPresent()) {
                // Get the value
                ICraftingRecipe recipe = (ICraftingRecipe)optionalRecipe.get();
                // If result inventory can use given recipe
                if (resultInventory.canUseRecipe(world, serverEntity, recipe))
                    // Sets empty stack as a result
                    stack = recipe.getCraftingResult(craftingInventory);
            }
            // Sets stack to slot with index 0(result slot)
            craftingInventory.setInventorySlotContents(0, stack);
            // Sends server player entity a slot packet
            serverEntity.connection.sendPacket(new SSetSlotPacket(index, 0, stack));
        }
    }
    /**
     * Event when matrix is changed
     * @param inventory Inventory
     */
    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        callable.consume((world, pos) -> {
            updateCraftingResult(windowId, world, player, matrix, result);
        });
    }
    /**
     * Fills stacked contents.
     * @param itemHelper Recipe item helper.
     */
    @Override
    public void fillStackedContents(RecipeItemHelper itemHelper) {
        matrix.fillStackedContents(itemHelper);
    }
    /**
     * Clears whole container.
     */
    @Override
    public void clear() {
        matrix.clear();
        result.clear();
    }

    /**
     * If both matricies match.
     * @param other Other matrix
     * @return If both matricies match.
     */
    @Override
    public boolean matches(IRecipe<? super CraftingInventory> other) {
        return other.matches(matrix, player.world);
    }
    /**
     * Event when container gets closed
     * @param playerEntity Entity closing this container
     */
    @Override
    public void onContainerClosed(PlayerEntity playerEntity) {
        // Calls base's onContainerClosed event
        super.onContainerClosed(playerEntity);
        // Consumes the container
        callable.consume((world, pos) -> clearContainer(playerEntity, world, matrix));
    }
    /**
     * Whether this container can interact with a player.
     * @param playerEntity Player entity.
     * @return Boolean.
     */
    @Override
    public boolean canInteractWith(PlayerEntity playerEntity) {
        return isWithinUsableDistance(callable, playerEntity, Blocks.CRAFTING_TABLE);
    }

    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int slot) {
        // TODO: Document this Mojang's code.
        ItemStack stack = ItemStack.EMPTY;
        Slot invSlot = inventorySlots.get(slot);
        if (invSlot != null && invSlot.getHasStack()) {
            ItemStack stack1 = invSlot.getStack();
            stack = stack1.copy();
            if(slot == 0) {
                callable.consume((world, pos) -> stack1.getItem().onCreated(stack1, world, playerEntity));
                if (!mergeItemStack(stack1, 10, 46, true)) return ItemStack.EMPTY;
                invSlot.onSlotChange(stack1, stack);
            }
            // Mojang, the actual fuck is this?
            else if (slot >= 10 && slot < 46)
                if (!mergeItemStack(stack1, 1, 10, false))
                    if (slot < 37)
                        if (!mergeItemStack(stack1, 37, 46, false)) return ItemStack.EMPTY;
                    else if (!this.mergeItemStack(stack1, 10, 37, false)) return ItemStack.EMPTY;
            else if (!mergeItemStack(stack1, 10, 46, false)) return ItemStack.EMPTY;

            if (stack1.isEmpty()) invSlot.putStack(ItemStack.EMPTY);
            else invSlot.onSlotChanged();
            // But... why?
            if (stack1.getCount() == stack.getCount()) return ItemStack.EMPTY;
            // This is practically useless and stupid:
            //ItemStack stack2 = slot.onTake(playerEntity, stack1);
            // onTake returns same stack you gave it... And mojang is creating new variable for that
            if (slot == 0) playerEntity.dropItem(stack1, false); // stack2 instead of stack1 if bug occurs
        }
        return stack;
    }

    /**
     * If slot can be merged with a stack.
     * @param stack Stack to merge
     * @param slot Slot to merge
     * @return If it can merge
     */
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != result && super.canMergeSlot(stack, slot);
    }

    /**
     * Gets output slot's index.
     * @return Always 0
     */
    public int getOutputSlot() {
        return 0;
    }
    /**
     * Gets width of the container.
     * @return Matrix width
     */
    public int getWidth() {
        return matrix.getWidth();
    }
    /**
     * Gets height of the container.
     * @return Matrix height
     */
    public int getHeight() {
        return matrix.getHeight();
    }
    /**
     * Gets size of...?
     * @return Size
     */
    @OnlyIn(Dist.CLIENT)
    public int getSize() {
        return 10;
    }
    /**
     * Recipe book category.
     * @return CRAFTING
     */
    @OnlyIn(Dist.CLIENT)
    public RecipeBookCategory func_241850_m() {
        return RecipeBookCategory.CRAFTING;
    }
}