package mods.computercarts.common.blocks;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
    public static Block NETWORK_RAIL;
    public static Block NETWORK_RAIL_BASE;

    public static void init() {
        GameRegistry.registerTileEntity(TileEntityNetworkRailBase.class, new ResourceLocation(ComputerCarts.MODID, "network_rail_base"));

        NETWORK_RAIL = registerBlock(new NetworkRail());
        NETWORK_RAIL_BASE = registerBlock(new NetworkRailBase());
    }

    private static <B extends Block> B registerBlock(B block) {
        ForgeRegistries.BLOCKS.register(block.setCreativeTab(ComputerCarts.TAB));
        return block;
    }
}
