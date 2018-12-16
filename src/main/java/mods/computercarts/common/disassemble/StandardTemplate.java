package mods.computercarts.common.disassemble;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//Standard Template for all Items of this mod.
public class StandardTemplate {
    public static boolean select(ItemStack stack) {
        Item item = stack.getItem();
        if (isOnBlacklist(item)) return false;
        return item.getRegistryName().getResourceDomain().equalsIgnoreCase(ComputerCarts.MODID);
    }

    public static ItemStack[] disassemble(ItemStack stack, ItemStack[] ingredients) {
        return ingredients; //Just return the items from the crafting recipe.
    }

    //Some items (like the computer cart) are not craftable and will return nothing
    //In this case we don't want to handle them on the normal way.
    private static boolean isOnBlacklist(Item item) {
        return item == ModItems.COMPUTER_CART;
    }
}
