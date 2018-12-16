package mods.computercarts.common.assemble.util;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Slot;
import net.minecraft.inventory.IInventory;

import java.util.Objects;

public class ComponentCheck {

    public static String checkRequiredComponents(IInventory inventory) {
        boolean hasCPU = false, hasRAM = false;

        for (int i = 0; i < inventory.getSizeInventory(); i += 1) {
            DriverItem drv = Driver.driverFor(inventory.getStackInSlot(i));
            if (drv != null) {
                String type = drv.slot(inventory.getStackInSlot(i));
                if (Objects.equals(type, Slot.CPU)) hasCPU = true;
                else if (Objects.equals(type, Slot.Memory)) hasRAM = true;
            }
        }

        if (!hasCPU) return ("Insert a CPU");
        if (!hasRAM) return ("Insert some Memory");
        return null;
    }
}
