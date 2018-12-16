package mods.computercarts.common.driver;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.server.component.UpgradeTankController;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.minecart.IComputerCart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DriverTankController implements DriverItem, HostAware, EnvironmentProvider {

    @Override
    public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
        if (IComputerCart.class.isAssignableFrom(host)) return this.worksWith(stack);
        return false;
    }

    @Override
    public boolean worksWith(ItemStack stack) {
        return !stack.isEmpty() && ModItems.getOCItem("tankcontrollerupgrade").isItemEqual(stack);
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
        return CustomDriver.dataTag(stack);
    }

    @Override
    public Class<?> getEnvironment(ItemStack stack) {
        if (!stack.isEmpty() && ModItems.getOCItem("inventorycontrollerupgrade").isItemEqual(stack))
            return UpgradeTankController.Drone.class;
        return null;
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        return host instanceof EntityComputerCart ? new UpgradeTankController.Drone((EnvironmentHost & Agent)host) : null;
    }
}
