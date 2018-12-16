package mods.computercarts.common.recipe.event;

import li.cil.oc.api.Items;
import mods.computercarts.common.items.ItemComputerCart;
import mods.computercarts.common.items.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mods.computercarts.common.recipe.Recipes.SLOT_ROM;

public class ComputerCartRomCrafting implements ICraftingToolHandler {

    @Override
    public boolean match(IInventory grid, ItemStack result) {
        return this.findResult(grid) != -1 && this.findRom(grid) != -1 && !this.hasUselessItem(grid)
                && result != null && result.getItem() == ModItems.COMPUTER_CART;
    }

    @Override
    public List<ItemStack> getItems(IInventory grid, ItemStack result) {
        List<ItemStack> list = new ArrayList<>();
        Map<Integer, ItemStack> comp = ((ItemComputerCart) ModItems.COMPUTER_CART).getComponents(grid.getStackInSlot(this.findResult(grid)));
        if (comp.containsKey(SLOT_ROM)) {
            ItemStack rom = comp.get(SLOT_ROM);
            rom.setCount(1);
            list.add(rom);
        }
        return list;

    }

    private int findRom(IInventory grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && Items.get(stack) == Items.get("eeprom"))
                return i;
        }
        return -1;
    }

    private int findResult(IInventory grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.COMPUTER_CART)
                return i;
        }
        return -1;
    }

    private boolean hasUselessItem(IInventory grid) {
        int rom = this.findRom(grid);
        int res = this.findResult(grid);
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && i != rom && i != res)
                return true;
        }
        return false;
    }

}
