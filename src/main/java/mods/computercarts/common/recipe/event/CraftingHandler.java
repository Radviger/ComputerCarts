package mods.computercarts.common.recipe.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CraftingHandler {
    private static List<ICraftingToolHandler> handlers = new ArrayList<>();

    public static void registerNewHandler(ICraftingToolHandler handler) {
        if (handlers.contains(handler)) return;
        handlers.add(handler);
    }

    public static void onCraftingEvent(PlayerEvent.ItemCraftedEvent event) {
        Iterator<ICraftingToolHandler> list = handlers.iterator();
        List<ItemStack> items = new ArrayList<>();
        while (list.hasNext()) {
            ICraftingToolHandler handler = list.next();
            if (handler.match(event.craftMatrix, event.crafting)) {
                List<ItemStack> ls = handler.getItems(event.craftMatrix, event.crafting);
                if (ls != null) items.addAll(ls);
            }
        }

        for (ItemStack item : items) {
            event.player.inventory.addItemStackToInventory(item);
        }
    }
}
