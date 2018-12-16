package mods.computercarts.common.items.interfaces;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

public interface ComponentInventoryItem {
    void setComponents(ItemStack stack, Int2ObjectMap<ItemStack> components);

    Int2ObjectMap<ItemStack> getComponents(ItemStack stack);
}
