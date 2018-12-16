package mods.computercarts.common.assemble.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mods.computercarts.Settings;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.util.ComputerCartData;
import mods.computercarts.common.util.ItemUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class General {
    public static Object[] validate(IInventory inventory, int maxcomplexity) {
        String need = ComponentCheck.checkRequiredComponents(inventory);
        if (need != null) {
            ITextComponent[] warnings = {new TextComponentString(need + "!")};
            return new Object[]{false, new TextComponentString(TextFormatting.RED + need), warnings};
        }
        if (Complexity.calculate(inventory) > maxcomplexity) {
            return new Object[]{false, new TextComponentString(TextFormatting.RED + "Complexity: " + Complexity.calculate(inventory) + " / " + maxcomplexity)};
        }
        return new Object[]{true, new TextComponentString("Complexity: " + Complexity.calculate(inventory) + " / " + maxcomplexity)};
    }

    public static ItemStack createItemStack(int tier, Int2ObjectMap<ItemStack> components) {
        ItemStack stack = new ItemStack(ModItems.COMPUTER_CART, 1);
        ComputerCartData data = new ComputerCartData();
        data.setComponents(components);
        data.setTier(tier);
        data.setEnergy(Settings.ComputerCartCreateEnergy);

        ItemUtil.setCartData(stack, data);
        return stack;
    }

    public static Object[] assemble(IInventory inventory, int tier) {
        Int2ObjectMap<ItemStack> comp = new Int2ObjectOpenHashMap<>();
        int size = inventory.getSizeInventory();
        for (int i = 1; i < size; i += 1) {
            if (!inventory.getStackInSlot(i).isEmpty()) comp.put(i - 1, inventory.getStackInSlot(i));
        }

        int energy = (tier < 3) ? Settings.ComputerCartBaseCost + Settings.ComputerCartComplexityCost * Complexity.calculate(inventory) : 0;

        return new Object[]{General.createItemStack(tier, comp), energy};
    }
}
