package com.bloodycrow.blocks;

import com.bloodycrow.tileentities.EnergyProducerTileEntity;
import com.bloodycrow.tileentities.EnergyReceiverTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class EnergyReceiverBlock extends Block {
    public EnergyReceiverBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!worldIn.isRemote && te instanceof EnergyProducerTileEntity) {
            if(!stack.isEmpty() && stack.hasDisplayName())
                ((EnergyReceiverTileEntity)te).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof INamedContainerProvider) {
                NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider)te, te.getPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
            return ActionResultType.SUCCESS;
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyReceiverTileEntity();
    }
}
