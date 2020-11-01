package com.randomteam;

import com.randomteam.blocks.ArcaneCrafterBlock;
import com.randomteam.blocks.EnergyProducerBlock;
import com.randomteam.containers.ArcaneCrafterContainer;
import com.randomteam.containers.EnergyProducerContainer;
import com.randomteam.containers.EnergyProducerScreen;
import com.randomteam.list.BlockList;
import com.randomteam.list.ContainerList;
import com.randomteam.list.ItemList;
import com.randomteam.list.TileEntityList;
import com.randomteam.tileentities.ArcaneCrafterTileEntity;
import com.randomteam.tileentities.EnergyProducerTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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

    public Quantia() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class, this::registerItem);
        bus.addGenericListener(Block.class, this::registerBlock);
        bus.addGenericListener(TileEntityType.class, this::registerTileEntity);
        bus.addGenericListener(ContainerType.class, this::registerContainer);
    }

    private void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ItemList.energy_producer = (BlockItem)new BlockItem(BlockList.energy_producer, new Item.Properties().group(ItemGroup.MISC)).setRegistryName(BlockList.energy_producer.getRegistryName()),
                ItemList.arcane_crafter = (BlockItem)new BlockItem(BlockList.arcane_crafter, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(BlockList.arcane_crafter.getRegistryName())
        );
    }

    private void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                BlockList.energy_producer = (EnergyProducerBlock)new EnergyProducerBlock(AbstractBlock.Properties.create(Material.WOOD).hardnessAndResistance(1.5F, 1.0F).setRequiresTool().harvestTool(ToolType.AXE).harvestLevel(1)).setRegistryName(new ResourceLocation(MOD_ID, "energy_producer")),
                BlockList.arcane_crafter = (ArcaneCrafterBlock)new ArcaneCrafterBlock(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(3F, 3F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
    }

    private void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(
                TileEntityList.energy_producer = (TileEntityType<EnergyProducerTileEntity>)TileEntityType.Builder.create(EnergyProducerTileEntity::new, BlockList.energy_producer).build(null).setRegistryName(new ResourceLocation(MOD_ID,"energy_producer")),
                TileEntityList.arcane_crafter = (TileEntityType<ArcaneCrafterTileEntity>)TileEntityType.Builder.create((ArcaneCrafterTileEntity::new, BlockList.arcane_crafter).build(null).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
    }

    private void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                ContainerList.energy_producer = (ContainerType<EnergyProducerContainer>)IForgeContainerType.create((windowId, inv, data) -> {
                    BlockPos pos = data.readBlockPos();
                    World world = inv.player.getEntityWorld();
                    return new EnergyProducerContainer(windowId, world, pos, inv, inv.player);
                }).setRegistryName(new ResourceLocation(MOD_ID, "energy_producer")),
                ContainerList.arcane_crafter = (ContainerType<ArcaneCrafterContainer>)IForgeContainerType.create((windowId, inv, data) -> {
                    BlockPos pos = data.readBlockPos();
                    World world = inv.player.getEntityWorld();
                    return new ArcaneCrafterContainer(windowId, inv, IWorldPosCallable.of(world, pos));
                }).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
        ScreenManager.registerFactory(ContainerList.energy_producer, EnergyProducerScreen::new);
        ScreenManager.registerFactory(ContainerList.arcane_crafter, ArcaneCrafterScreen::new);
    }
}
