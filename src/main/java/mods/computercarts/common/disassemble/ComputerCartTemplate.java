package mods.computercarts.common.disassemble;

import li.cil.oc.common.Tier;
import mods.computercarts.common.items.ItemComputerCart;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.util.ComputerCartData;
import mods.computercarts.common.util.ItemUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ComputerCartTemplate {

    public static boolean select(ItemStack stack) {
        return stack.getItem() == ModItems.COMPUTER_CART;
    }

    public static ItemStack[] disassemble(ItemStack stack, ItemStack[] ingredients) {
        List<ItemStack> list = new ArrayList<>();

        ComputerCartData data = ItemUtil.getCartData(stack);
        if (data == null) return new ItemStack[]{};
        if (stack.getItem() == ModItems.COMPUTER_CART) {
            if (data.getTier() < 3 && data.getTier() != Tier.None())
                list.add(new ItemStack(ModItems.COMPUTER_CART_CASE, 1, data.getTier()));

            if (data.getComponents() != null) {
                list.addAll(data.getComponents().values());
            }
        }
        ItemStack[] items = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i += 1) items[i] = list.get(i);
        return items;
    }
}
