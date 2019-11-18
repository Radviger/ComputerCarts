package mods.computercarts.client.manual;

import li.cil.oc.api.manual.PathProvider;
import mods.computercarts.ComputerCarts;
import mods.computercarts.common.blocks.ModBlocks;
import mods.computercarts.common.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManualPathProvider implements PathProvider {

    private static final String PATH_PREFIX = ComputerCarts.MODID + "/%LANGUAGE%/";

    @Override
    public String pathFor(ItemStack stack) {
        if (stack == null) return null;
        Item item = stack.getItem();

        if (item == ModItems.COMPUTER_CART_CASE) return PATH_PREFIX + "item/cartcase.md";
        else if (item == ModItems.COMPUTER_CART) return PATH_PREFIX + "item/cart.md";
        else if (item == ModItems.CART_REMOTE_MODULE) return PATH_PREFIX + "item/remote.md";
        else if (item == ModItems.CART_REMOTE_ANALYZER) return PATH_PREFIX + "item/remoteanalyzer.md";
        else if (item == ModItems.LINKING_UPGRADE) return PATH_PREFIX + "item/linkingupgrade.md";

        return null;
    }

    @Override
    public String pathFor(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == ModBlocks.NETWORK_RAIL_CONTROLLER) return PATH_PREFIX + "block/netrailbase.md";
        else if (block == ModBlocks.NETWORK_RAIL) return PATH_PREFIX + "block/netrail.md";

        return null;
    }

}
