package mods.computercarts.common.assemble;

import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.common.Tier;
import mods.computercarts.common.assemble.util.General;
import mods.computercarts.common.items.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class ComputerCartT2Template {
    private static final int MAX_COMPLEXITY = 100;

    public static boolean select(ItemStack stack) {
        return stack.getItem().equals(ModItems.COMPUTER_CART_CASE) && stack.getItemDamage() == 1;
    }

    public static Object[] validate(IInventory inventory) {
        return General.validate(inventory, MAX_COMPLEXITY);
    }

    public static Object[] assemble(IInventory inventory) {
        return General.assemble(inventory, 1);
    }

    public static int[] getContainerTier() {
        return new int[]{2, 1, 0};
    }

    public static int[] getUpgradeTier() {
        return new int[]{1, 1, 1, 0, 0, 0};
    }

    public static Iterable<Pair<String, Integer>> getComponentSlots() {
        ArrayList<Pair<String, Integer>> list = new ArrayList<Pair<String, Integer>>();
        list.add(Pair.of(Slot.Card, 1));
        list.add(Pair.of(Slot.Card, 0));
        list.add(Pair.of(Slot.None, -1));

        list.add(Pair.of(Slot.CPU, 1));
        list.add(Pair.of(Slot.Memory, 1));
        list.add(Pair.of(Slot.Memory, 1));

        list.add(Pair.of("eeprom", Tier.Any()));
        list.add(Pair.of(Slot.HDD, 1));
        return list;
    }
}
