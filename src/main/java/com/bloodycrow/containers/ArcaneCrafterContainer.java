package com.bloodycrow.containers;

import com.bloodycrow.list.BlockList;
import com.bloodycrow.list.ContainerList;
import com.bloodycrow.list.RecipeList;
import com.bloodycrow.recipes.ArcaneCrafterRecipe;
import com.bloodycrow.util.DummyContainer;
import com.bloodycrow.util.HandlerWrapper;
import com.bloodycrow.util.Util;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ArcaneCrafterContainer extends Container {
    private final IWorldPosCallable callable;
    private final World world;
    private final TileEntity te;
    /**
     * A wrapper for a handler, so we can use IRecipe shit with. The actual inventory is a capability within a tile entity, and to actually get items you need to use {@link Container#getSlot(int)}.
     */
    private HandlerWrapper inv;
    private Either<ICraftingRecipe, ArcaneCrafterRecipe> currentRecipe;
    /**
     * Used for methods to get normal crafting recipes.
     */
    private CraftingInventory tempInventory;

    public ArcaneCrafterContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, IWorldPosCallable callable) {
        super(ContainerList.arcane_crafter, windowId);
        int[] positions = new int[2];
        this.world = world;
        te = world.getTileEntity(pos);
        this.callable = callable;

        // Draws 3x3 grid and sets up some final things.
        if(te != null) {
            te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                //The handler should always be present, meaning this is never null. If not something is very broken
                inv = new HandlerWrapper((IItemHandlerModifiable)h);
                assertInventorySize(new Inventory(Util.toArray(h)), 26);
                int index = 0;
                addSlot(new SlotItemHandler(h, index, 147, 53) { // 137, 35
                    @Override
                    public boolean isItemValid(@Nonnull ItemStack stack) {
                        return false;
                    }

                    @Override
                    public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                        return onCraftingResultItemTakenFromSlot(thePlayer, stack);
                    }

                    @Override
                    public void onSlotChanged() {
                        determineRecipe();
                    }
                });
                index = 1;
                for(positions[0] = 0; positions[0] < 5; positions[0]++) { // 5 rows
                    for (positions[1] = 0; positions[1] < 5; positions[1]++) { // 5 columns
                        addSlot(new SlotItemHandler(h, index++, 12 + positions[1] * 18, 17 + positions[0] * 18) {
                            @Override
                            public void onSlotChanged() {
                                determineRecipe();
                            }
                        });
                    }
                }
            });
        }
        // Player's inventory
        for(positions[0] = 0; positions[0] < 3; positions[0]++) // 3 rows
            for(positions[1] = 0; positions[1] < 9; positions[1]++) // 9 columns
                addSlot(new Slot(inventory, positions[1] + positions[0] * 9 + 9, 8 + positions[1] * 18, 120 + positions[0] * 18)); // 84 + ...
        // Player's hotbar
        for(positions[0] = 0; positions[0] < 9; positions[0]++) // 1 row, 9 columns
            addSlot(new Slot(inventory, positions[0], 8 + positions[0] * 18, 178)); // ..., 142
    }

    /**
     * Whether this container can interact with a player.
     * @param playerEntity Player entity.
     * @return Boolean.
     */
    @Override
    public boolean canInteractWith(PlayerEntity playerEntity) {
        return isWithinUsableDistance(callable, playerEntity, BlockList.arcane_crafter);
    }

    /**
     * When the container is closed.
     * @param playerIn The player that closed the container
     */
//    @Override
//    public void onContainerClosed(PlayerEntity playerIn) {
//        super.onContainerClosed(playerIn);
//        //for(int i = 1; i < 10; i++) {
//        //    ItemStack toDrop = getSlot(i).getStack();
//        //    if(!toDrop.isEmpty())
//        //        playerIn.dropItem(toDrop, false);
//        //}
//    }

    /**
     * Used to determine if a crafting recipe is valid.
     */
    private void determineRecipe() {
        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            updateInv();
            ICraftingRecipe recipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, tempInventory, world).orElse(null);
            currentRecipe = Either.left(recipe);
            if(recipe != null)
                ((IItemHandlerModifiable)h).setStackInSlot(0, recipe.getCraftingResult(tempInventory));
            else {
                world.getRecipeManager().getRecipe(RecipeList.arcane_crafter, inv, world).ifPresent(arcaneRecipe -> {
                    ((IItemHandlerModifiable)h).setStackInSlot(0, arcaneRecipe.getCraftingResult(inv));
                    currentRecipe = Either.right(arcaneRecipe);
                });
            }
        });
    }

    /**
     * When the item from the result slot gets taken by a player.
     * @return Always the inputted stack.
     */
    private ItemStack onCraftingResultItemTakenFromSlot(PlayerEntity player, ItemStack stack) {
        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            updateInv();
            //Gets the remaining items from the recipe when you don't have enough inventory space.
            //We're using a single element array because consumers need effectively final or atomic variables...
            final NonNullList<ItemStack>[] remainingItems = new NonNullList[1];
            if(currentRecipe != null)
                currentRecipe.ifLeft(craftingRecipe -> remainingItems[0] = getRemainingItems(craftingRecipe)).ifRight(arcaneRecipe -> remainingItems[0] = getRemainingItems(arcaneRecipe));
            ((IItemHandlerModifiable)h).setStackInSlot(0, ItemStack.EMPTY);
            for(int i = 1; i < 26; i++) {
                ItemStack stack1 = h.getStackInSlot(i);
                stack1.shrink(1);
                ((IItemHandlerModifiable)h).setStackInSlot(i, stack1);
            }
            for(ItemStack stack1 : remainingItems[0]) {
                if(!stack1.isEmpty())
                    if(!player.inventory.addItemStackToInventory(stack1))
                        player.dropItem(stack1, false);
            }
            determineRecipe();
        });
        return stack;
    }

    private NonNullList<ItemStack> getRemainingItems(IRecipe<?> recipe) {
        try {
            return recipe instanceof ICraftingRecipe ? ((ICraftingRecipe)recipe).getRemainingItems(tempInventory) : ((ArcaneCrafterRecipe)recipe).getRemainingItems(inv);
        } catch(NullPointerException e) {
            return NonNullList.withSize(26, ItemStack.EMPTY);
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        determineRecipe();
    }

    private void updateInv() {
        tempInventory = new CraftingInventory(new DummyContainer(), 3, 3);
        for(int i = 0; i < 8; i++) {
            tempInventory.setInventorySlotContents(i, getSlot(i).getStack());
            inv.setInventorySlotContents(i, getSlot(i).getStack());
        }
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
                if (!mergeItemStack(stack1, 26, 62, true)) return ItemStack.EMPTY; // 10 => 26, 46 => 62
                invSlot.onSlotChange(stack1, stack);
            }
            // Mojang, the actual fuck is this?
            else if (slot >= 11 && slot < 47)
                if (!mergeItemStack(stack1, 1, 26, false)) // 10 => 26
                    if (slot < 37)
                        if (!mergeItemStack(stack1, 54, 63, false)) return ItemStack.EMPTY; // 38 => 54, 47 => 63
                    else if (!this.mergeItemStack(stack1, 27, 54, false)) return ItemStack.EMPTY; // 11 => 27, 38 => 54
            else if (!mergeItemStack(stack1, 27, 63, false)) return ItemStack.EMPTY; // 11 => 27, 47 => 63

            if (stack1.isEmpty()) invSlot.putStack(ItemStack.EMPTY);
            else invSlot.onSlotChanged();
            // But... why?
            if (stack1.getCount() == stack.getCount()) return ItemStack.EMPTY;
            /* This is practically useless and stupid:
            /  onTake returns same stack you gave it... And mojang is creating new variable for that
            /  Commented line: ItemStack stack2 = slot.onTake(playerEntity, stack1); */
            // Drop the item if theres no space.
            if (slot == 0) playerEntity.dropItem(stack1, false); // stack2 instead of stack1 if bug occurs
        }
        return stack;
    }
}