package mods.computercarts.common.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import li.cil.oc.api.Items;
import mods.computercarts.common.items.interfaces.ComponentInventoryItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RomCrafting extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    @Nonnull
    private final ItemStack output;
    private final int romslot;

    public RomCrafting(@Nonnull ItemStack output, int romslot) {
        this.output = output;
        this.romslot = romslot;
        this.setRegistryName("rom");
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world) {
        return this.findResult(grid) != -1 && this.findRom(grid) != -1 && !this.hasUselessItem(grid);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting grid) {
        if (this.output.getItem() instanceof ComponentInventoryItem) {
            if (this.findResult(grid) == -1 || this.findRom(grid) == -1 || this.hasUselessItem(grid))
                return ItemStack.EMPTY;
            int rom = this.findRom(grid);
            int res = this.findResult(grid);
            ItemStack host = grid.getStackInSlot(res).copy();
            ItemStack oldRom = ItemStack.EMPTY;
            Int2ObjectMap<ItemStack> comp = ((ComponentInventoryItem)host.getItem()).getComponents(host);
            if (comp == null) return ItemStack.EMPTY;
            if (comp.containsKey(this.romslot)) oldRom = comp.get(this.romslot);
            ItemStack newRom = grid.getStackInSlot(rom).copy();
            newRom.setCount(1);
            host.setCount(1);
            comp.put(this.romslot, newRom);
            ((ComponentInventoryItem)host.getItem()).setComponents(host, comp);
            return host;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int w, int h) {
        return w >= 2 && h >= 2;
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        ItemStack out = this.output.copy();
        if (out.getItem() instanceof ComponentInventoryItem) {
            out.setCount(1);
            return out;
        }
        return ItemStack.EMPTY;
    }

    private int findRom(InventoryCrafting grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && (Items.get(stack) == Items.get("eeprom")))
            return i;
        }
        return -1;
    }

    private int findResult(InventoryCrafting grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isItemEqual(this.output))
                return i;
        }
        return -1;
    }

    private boolean hasUselessItem(InventoryCrafting grid) {
        int rom = this.findRom(grid);
        int res = this.findResult(grid);
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty() && i != rom && i != res)
                return true;
        }
        return false;
    }
}
