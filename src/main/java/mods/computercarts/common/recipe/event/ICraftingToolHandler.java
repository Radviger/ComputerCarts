package mods.computercarts.common.recipe.event;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ICraftingToolHandler {
    boolean match(IInventory grid, ItemStack result);

    List<ItemStack> getItems(IInventory grid, ItemStack result);
}
