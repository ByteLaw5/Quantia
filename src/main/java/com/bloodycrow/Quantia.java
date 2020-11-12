package com.bloodycrow;

import com.bloodycrow.blocks.ArcaneCrafterBlock;
import com.bloodycrow.containers.ArcaneCrafterContainer;
import com.bloodycrow.screens.ArcaneCrafterScreen;
import com.bloodycrow.entity.TestInvaderEntity;
import com.bloodycrow.entity.client.TestInvaderEntityRenderer;
import com.bloodycrow.invaderraid.InvaderRaid;
import com.bloodycrow.invaderraid.InvaderRaidManager;
import com.bloodycrow.list.*;
import com.bloodycrow.networking.QuantiaNetwork;
import com.bloodycrow.recipes.ArcaneCrafterRecipe;
import com.bloodycrow.tileentities.ArcaneCrafterTileEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER);
        QuantiaNetwork.registerMessages();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class, this::registerItem);
        bus.addGenericListener(Block.class, this::registerBlock);
        bus.addGenericListener(TileEntityType.class, this::registerTileEntity);
        bus.addGenericListener(ContainerType.class, this::registerContainer);
        bus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializer);
        bus.addGenericListener(EntityType.class, this::registerEntity);
        bus.addListener(this::setupEvent);
        if(FMLEnvironment.dist == Dist.CLIENT)
            bus.addListener(this::clientEvent);
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(this::worldTickEvent);
        forge.addListener(this::registerCommands);
    }

    private void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ItemList.arcane_crafter = (BlockItem)new BlockItem(BlockList.arcane_crafter, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.arcane_crafter.getRegistryName()),
                ItemList.corrupted_bricks = (BlockItem)new BlockItem(BlockList.corrupted_bricks, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.corrupted_bricks.getRegistryName()),
                ItemList.cracked_corrupted_bricks = (BlockItem)new BlockItem(BlockList.cracked_corrupted_bricks, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.cracked_corrupted_bricks.getRegistryName()),
                ItemList.mossy_corrupted_bricks = (BlockItem)new BlockItem(BlockList.mossy_corrupted_bricks, new Item.Properties().group(QUANTIA)).setRegistryName(BlockList.mossy_corrupted_bricks.getRegistryName())
        );
    }

    private void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                BlockList.arcane_crafter = (ArcaneCrafterBlock)new ArcaneCrafterBlock(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(3F, 3F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter")),
                BlockList.corrupted_bricks = new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GRAY).hardnessAndResistance(2.75F, 6.35F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "corrupted_bricks")),
                BlockList.cracked_corrupted_bricks = new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GRAY).hardnessAndResistance(2.75F, 6.35F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "cracked_corrupted_bricks")),
                BlockList.mossy_corrupted_bricks = new Block(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.GRAY).hardnessAndResistance(2.75F, 6.35F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)).setRegistryName(new ResourceLocation(MOD_ID, "mossy_corrupted_bricks"))
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
                    return new ArcaneCrafterContainer(windowId, world, pos, inv, IWorldPosCallable.of(world, pos));
                }).setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
        ScreenManager.registerFactory(ContainerList.arcane_crafter, ArcaneCrafterScreen::new);
    }

    private void registerRecipeSerializer(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        //Also registering types here.
        RecipeList.arcane_crafter = Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MOD_ID, "arcane_crafter"), new IRecipeType<ArcaneCrafterRecipe>(){
            @Override
            public String toString() {
                return "quantia:arcane_crafter";
            }
        });
        event.getRegistry().registerAll(
                RecipeSerializerList.arcane_recipe_serializer = (ArcaneCrafterRecipe.Serializer)new ArcaneCrafterRecipe.Serializer().setRegistryName(new ResourceLocation(MOD_ID, "arcane_crafter"))
        );
    }

    private void registerEntity(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(
                EntityList.test_invader = (EntityType<TestInvaderEntity>)EntityType.Builder.create(TestInvaderEntity::new, EntityClassification.MONSTER).build("quantia:test_invader").setRegistryName(new ResourceLocation(MOD_ID, "test_invader"))
        );
    }

    private void clientEvent(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityList.test_invader, TestInvaderEntityRenderer::new);
    }

    private void setupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GlobalEntityTypeAttributes.put(EntityList.test_invader, TestInvaderEntity.createAttributes().create());
        });
    }

    private void worldTickEvent(TickEvent.WorldTickEvent worldTickEvent) {
        if(worldTickEvent.phase == TickEvent.Phase.START && worldTickEvent.side == LogicalSide.SERVER) {
            InvaderRaidManager.getForWorld((ServerWorld)worldTickEvent.world).tick();
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("startraid")
                .requires(source -> source.hasPermissionLevel(3))
                .executes(ctx -> {
                    InvaderRaid raid = InvaderRaidManager.createRaid(ctx.getSource().getWorld(), ctx.getSource().assertIsEntity().getPosition());
                    return 0;
                }));
    }

    public static class Config {
        public static final ForgeConfigSpec SERVER;

        public static final ForgeConfigSpec.IntValue RAID_DELAY;
        public static final ForgeConfigSpec.BooleanValue ENABLE_RAIDS;

        static {
            ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
            serverBuilder.comment("Invader Raids").push("invaderraids");
            RAID_DELAY = serverBuilder.comment("The delay between invader raids").defineInRange("raidDelay", 72000, 0, Integer.MAX_VALUE);
            ENABLE_RAIDS = serverBuilder.comment("If you don't want raids, set this to false").define("enableRaids", true);
            serverBuilder.pop();
            SERVER = serverBuilder.build();
        }
    }
}
