package com.randomteam;

import com.randomteam.list.BlockList;
import com.randomteam.list.ItemList;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Quantia.MOD_ID)
public class Quantia {
    public static final String MOD_ID = "quantia";

    public Quantia() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class, this::registerItem);
        bus.addGenericListener(Block.class, this::registerBlock);
    }

    private void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ItemList.test_item = new Item(new Item.Properties().group(ItemGroup.MISC)) {
                    @Override
                    public ActionResultType onItemUse(ItemUseContext context) {
                        if(!context.getWorld().isRemote) {
                            context.getWorld().destroyBlock(context.getPos(), true, context.getPlayer());
                            return ActionResultType.SUCCESS;
                        }
                        return ActionResultType.PASS;
                    }
                }.setRegistryName(new ResourceLocation(MOD_ID, "test_item")),

                ItemList.test_block = (BlockItem)new BlockItem(BlockList.test_block, new Item.Properties().group(ItemGroup.MISC)).setRegistryName(BlockList.test_block.getRegistryName())
        );
    }

    private void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                /*
                From testing,
                0 = wood
                1 = stone
                2 = iron/gold
                3 = diamond
                 */
                BlockList.test_block = new Block(AbstractBlock.Properties.create(Material.IRON).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2).hardnessAndResistance(5.0F, 6.0F)).setRegistryName(new ResourceLocation(MOD_ID, "test_block"))
        );
    }
}
