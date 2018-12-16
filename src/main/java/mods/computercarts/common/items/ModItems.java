package mods.computercarts.common.items;

import li.cil.oc.api.Items;
import li.cil.oc.api.detail.ItemInfo;
import mods.computercarts.ComputerCarts;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ModItems {
    public static Item COMPUTER_CART;
    public static Item COMPUTER_CART_CASE;
    public static Item CART_REMOTE_MODULE;
    public static Item CART_REMOTE_ANALYZER;
    public static Item LINKING_UPGRADE;

    public static void init() {
        COMPUTER_CART = registerItem(new ItemComputerCart());
        COMPUTER_CART_CASE = registerItem(new ComputerCartCase());
        CART_REMOTE_MODULE = registerItem(new ItemCartRemoteModule());
        CART_REMOTE_ANALYZER = registerItem(new ItemRemoteAnalyzer());
        LINKING_UPGRADE = registerItem(new ItemLinkingUpgrade());

    }

    private static <I extends Item> I registerItem(I item) {
        ForgeRegistries.ITEMS.register(item.setCreativeTab(ComputerCarts.TAB));
        return item;
    }

    public static ItemStack getOCItem(String name) {
        ItemInfo info = Items.get(name);
        return info != null ? info.createItemStack(1) : ItemStack.EMPTY;
    }
}
