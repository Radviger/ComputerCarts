package mods.computercarts.common.driver;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import mods.computercarts.common.component.CraftingUpgradeCC;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.minecart.IComputerCart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DriverCraftingUpgrade implements DriverItem, HostAware, EnvironmentProvider {

    @Override
    public boolean worksWith(ItemStack stack) {
        return !stack.isEmpty() && ModItems.getOCItem("craftingupgrade").isItemEqual(stack);
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        if (IComputerCart.class.isAssignableFrom(host.getClass())) {
            return new CraftingUpgradeCC((IComputerCart) host);
        }
        return null;
    }

    @Override
    public String slot(ItemStack stack) {
        return Driver.driverFor(stack).slot(stack);
    }

    @Override
    public int tier(ItemStack stack) {
        return Driver.driverFor(stack).tier(stack);
    }

    @Override
    public NBTTagCompound dataTag(ItemStack stack) {
        return null;
    }

    @Override
    public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
        if (IComputerCart.class.isAssignableFrom(host)) return this.worksWith(stack);
        return false;
    }

    @Override
    public Class<?> getEnvironment(ItemStack stack) {
        return CraftingUpgradeCC.class;
    }

}
