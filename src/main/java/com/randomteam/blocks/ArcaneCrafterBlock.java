package com.randomteam.blocks;

import com.randomteam.containers.ArcaneCrafterContainer;
import com.randomteam.tileentities.ArcaneCrafterTileEntity;
import com.randomteam.tileentities.EnergyProducerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Class for Arcane Crafter.
 */
@SuppressWarnings("deprecation")
public class ArcaneCrafterBlock extends Block {
    /**
     * Name of the container for lang files.
     */
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.arcane_crafting");
    public ArcaneCrafterBlock(Properties properties) {
        super(properties);
    }
    /**
     * Creates tile entity of this block.
     * @param state State of the block
     * @param world World this block is in
     * @return EnergyProducer tile entity.
     */
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ArcaneCrafterTileEntity();
    }
    /**
     * If it already has tile entity.
     * @param state State of the block
     * @return Always true.
     */
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    /**
     * When the block is placed.
     * @param worldIn World this block is in
     * @param pos Position of the block
     * @param state State of the block
     * @param placer Who placed the block
     * @param stack Stack of the block.
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Get tile entity in this position
        TileEntity te = worldIn.getTileEntity(pos);
        if(!worldIn.isRemote && te instanceof ArcaneCrafterTileEntity) {
            if(!stack.isEmpty() && stack.hasDisplayName())
                // Sets custom name of it
                ((ArcaneCrafterTileEntity)te).setCustomName(stack.getDisplayName());
        }
    }
    /**
     * When block is activated/right clicked.
     * @param state State this block is in
     * @param world World this block is in
     * @param pos Position of the block
     * @param player Player which activated it
     * @param hand Hand used
     * @param ray Hit raytrace
     * @return Action result
     */
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if(!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            // If it's named container provider, open GUI
            if(te instanceof INamedContainerProvider) NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider)te, te.getPos());
                // Throw an error that it doesn't exist
            else throw new IllegalStateException("Could not find container provider of ArcaneCrafterBlock.");
            // Return action result as success
            return ActionResultType.SUCCESS;
        }
        // If world is remote, activate base class onBlockActivated
        return super.onBlockActivated(state, world, pos, player, hand, ray);
    }
}
