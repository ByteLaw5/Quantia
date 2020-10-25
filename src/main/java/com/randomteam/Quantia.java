package com.randomteam;

import com.randomteam.list.ItemList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
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
                }.setRegistryName(new ResourceLocation(MOD_ID, "test_item"))
        );
    }
}
