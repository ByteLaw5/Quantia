package com.bloodycrow;

import com.bloodycrow.blocks.ArcaneCrafterBlock;
import com.bloodycrow.containers.ArcaneCrafterContainer;
import com.bloodycrow.containers.ArcaneCrafterScreen;
import com.bloodycrow.list.BlockList;
import com.bloodycrow.list.ContainerList;
import com.bloodycrow.list.ItemList;
import com.bloodycrow.list.TileEntityList;
import com.bloodycrow.tileentities.ArcaneCrafterTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// TODO: Fix this whole mess
@Mod(Quantia.MOD_ID)
public class Quantia {
    public static final String MOD_ID = "quantia";
    public static final ItemGroup QUANTIA = new ItemGroup("quantia") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ItemList.corrupted_bricks);
        }
    };

    public Quantia() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class, this::registerItem);
        bus.addGenericListener(Block.class, this::registerBlock);
        bus.addGenericListener(TileEntityType.class, this::registerTileEntity);
        bus.addGenericListener(ContainerType.class, this::registerContainer);
    }

    private void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ItemList.arcane_crafter = (BlockItem)new BlockItem(BlockList.arcane_crafter, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.arcane_crafter.getRegistryName()),
                ItemList.corrupted_bricks = (BlockItem)new BlockItem(BlockList.corrupted_bricks, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.corrupted_bricks.getRegistryName()),
                ItemList.cracked_corrupted_bricks = (BlockItem)new BlockItem(BlockList.cracked_corrupted_bricks, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.cracked_corrupted_bricks.getRegistryName())
        );
    }

    private void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                BlockList.arcane_crafter = (ArcaneCrafterBlock)new ArcaneCrafterBlock(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(3F, 3F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter")),
                BlockList.corrupted_bricks = new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GRAY).hardnessAndResistance(2.75F, 6.35F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "corrupted_bricks")),
                BlockList.cracked_corrupted_bricks = new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GRAY).hardnessAndResistance(2.75F, 6.35F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "cracked_corrupted_bricks"))
        );
    }

    private void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(
                TileEntityList.arcane_crafter = (TileEntityType<ArcaneCrafterTileEntity>)TileEntityType.Builder.create(ArcaneCrafterTileEntity::new, BlockList.arcane_crafter).build(null).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
    }

    private void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                ContainerList.arcane_crafter = (ContainerType<ArcaneCrafterContainer>)IForgeContainerType.create((windowId, inv, data) -> {
                    BlockPos pos = data.readBlockPos();
                    World world = inv.player.getEntityWorld();
                    return new ArcaneCrafterContainer(windowId, inv, IWorldPosCallable.of(world, pos));
                }).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
        ScreenManager.registerFactory(ContainerList.arcane_crafter, ArcaneCrafterScreen::new);
    }
}
