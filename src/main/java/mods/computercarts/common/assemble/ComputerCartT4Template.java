package mods.computercarts.common.assemble;

import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.common.Tier;
import mods.computercarts.common.assemble.util.General;
import mods.computercarts.common.items.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class ComputerCartT4Template {

    private static final int MAX_COMPLEXITY = 9001;

    public static boolean select(ItemStack stack) {
        return stack.getItem().equals(ModItems.COMPUTER_CART_CASE) && stack.getItemDamage() == 3;
    }

    public static Object[] validate(IInventory inventory) {
        return General.validate(inventory, MAX_COMPLEXITY);
    }

    public static Object[] assemble(IInventory inventory) {
        return General.assemble(inventory, 3);
    }

    public static int[] getContainerTier() {
        return new int[]{2, 2, 2};
    }

    public static int[] getUpgradeTier() {
        return new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2};
    }

    public static Iterable<Pair<String, Integer>> getComponentSlots() {
        ArrayList<Pair<String, Integer>> list = new ArrayList<Pair<String, Integer>>();
        list.add(Pair.of(Slot.Card, 2));
        list.add(Pair.of(Slot.Card, 2));
        list.add(Pair.of(Slot.Card, 2));

        list.add(Pair.of(Slot.CPU, 2));
        list.add(Pair.of(Slot.Memory, 2));
        list.add(Pair.of(Slot.Memory, 2));

        list.add(Pair.of("eeprom", Tier.Any()));
        list.add(Pair.of(Slot.HDD, 2));
        return list;
    }
}
