package com.randomteam.blocks;

import com.randomteam.containers.ArcaneCrafterContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for Arcane Crafter.
 */
public class ArcaneCrafterBlock extends Block {
    /**
     * Name of the container for lang files.
     */
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.arcane_crafting");
    public ArcaneCrafterBlock(Properties properties) {
        super(properties);
    }
    /**
     * Event when Arcane Crafter gets activated/right clicked.
     * @param state State of the block
     * @param world World this block is in
     * @param pos Position of the block
     * @param entity Player entity which activated it
     * @param hand Hand used
     * @param traceResult Hit raytrace
     * @return Action result.
     */
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult traceResult) {
        // If world is remote, then return that it's fully done
        if(world.isRemote) return ActionResultType.SUCCESS;
        // If it isn't remote,
        // open container
        entity.openContainer(state.getContainer(world, pos));
        // Add status that the player is interacting with crafting table(it's actually interacting with Arcane Crafter)
        entity.addStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        // Return that it is activated
        return ActionResultType.CONSUME;
    }

    /**
     * Gets container of the Arcane Crafter.
     * @param state State of the Arcane Crafter
     * @param world World Arcane Crafter is in
     * @param pos Position of Arcane Crafter
     * @return Arcane Crafter container.
     */
    @Nullable
    @Override
    public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedContainerProvider(
                (smth, inv, player) -> new ArcaneCrafterContainer(smth, inv, IWorldPosCallable.of(world, pos)),
                CONTAINER_NAME
        );
    }
}
