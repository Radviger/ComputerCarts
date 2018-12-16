package mods.computercarts.common.driver;

import li.cil.oc.api.API;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.network.EnvironmentHost;
import mods.computercarts.Settings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/*
 * This is the best way I found to override the OC Item Drivers for the computer cart
 * without changing the Drivers for other Hosts (Computer, Robot, ...)
 */

public class CustomDriver {

    private static List<DriverItem> drivers = new ArrayList<>();

    public static void init() {
        CustomDriver.registerNewDriver(new DriverInventoryController());
        CustomDriver.registerNewDriver(new DriverTankController());
        CustomDriver.registerNewDriver(new DriverCraftingUpgrade());

        API.driver.add(new DriverLinkingUpgrade());
    }

    //Data tags for OC components
    public static NBTTagCompound dataTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey(Settings.OC_Namespace + "data")) {
            nbt.setTag(Settings.OC_Namespace + "data", new NBTTagCompound());
        }
        return nbt.getCompoundTag(Settings.OC_Namespace + "data");
    }

    //Add and get Drivers
    private static void registerNewDriver(DriverItem driver) {
        if (!CustomDriver.drivers.contains(driver)) {
            CustomDriver.drivers.add(driver);
        }
    }

    public static DriverItem driverFor(ItemStack stack, Class<? extends EnvironmentHost> host) {
        for (DriverItem driver : CustomDriver.drivers) {
            if ((driver instanceof HostAware) && ((HostAware) driver).worksWith(stack, host)) {
                return driver;
            }
        }
        return Driver.driverFor(stack, host);
    }

    public static DriverItem driverFor(ItemStack stack) {
        return Driver.driverFor(stack);
    }

}
