package mods.computercarts.common.assemble.util;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.common.Tier;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class Complexity {
    public static int calculate(Iterable<ItemStack> components) {
        int complexity = 0;
        for (ItemStack comp : components) {
            DriverItem drv = Driver.driverFor(comp);
            if (drv != null && drv.tier(comp) != Tier.None() && drv.tier(comp) != Tier.Any() && !Objects.equals(drv.slot(comp), "eeprom")) {
                complexity += drv.tier(comp) + 1;
            }
        }

        return complexity;
    }

    public static int calculate(IInventory inventory) {
        ArrayList<ItemStack> components = new ArrayList<ItemStack>();
        for (int i = 1; i < inventory.getSizeInventory(); i += 1) {
            components.add(inventory.getStackInSlot(i));
        }
        return calculate(components);
    }
}
