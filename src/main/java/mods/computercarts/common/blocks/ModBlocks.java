package mods.computercarts.common.blocks;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.function.Function;

public class ModBlocks {
    public static Block NETWORK_RAIL;
    public static Block NETWORK_RAIL_CONTROLLER;

    public static void init() {
        GameRegistry.registerTileEntity(TileEntityNetworkRailController.class, new ResourceLocation(ComputerCarts.MODID, "network_rail_controller"));

        NETWORK_RAIL = registerBlockWithItem(new BlockNetworkRail(), ItemBlock::new);
        NETWORK_RAIL_CONTROLLER = registerBlockWithItem(new BlockNetworkRailController(), ItemBlock::new);
    }

    private static <B extends Block> B registerBlock(B block) {
        ForgeRegistries.BLOCKS.register(block.setCreativeTab(ComputerCarts.TAB));
        return block;
    }

    private static <B extends Block, I extends ItemBlock> B registerBlockWithItem(B block, Function<B, I> item) {
        registerBlock(block);
        ForgeRegistries.ITEMS.register(item.apply(block).setRegistryName(block.getRegistryName()));
        return block;
    }
}
