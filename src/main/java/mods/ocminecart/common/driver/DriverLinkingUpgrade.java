package mods.ocminecart.common.driver;

import li.cil.oc.api.driver.Item;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import mods.ocminecart.common.component.LinkingUpgrade;
import mods.ocminecart.common.items.ItemLinkingUpgrade;
import mods.ocminecart.common.minecart.IComputerCart;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class DriverLinkingUpgrade implements Item, HostAware {

	@Override
	public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
		return IComputerCart.class.isAssignableFrom(host) && EntityMinecart.class.isAssignableFrom(host) && worksWith(stack);
	}

	@Override
	public boolean worksWith(ItemStack stack) {
		return (stack.getItem() instanceof ItemLinkingUpgrade);
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack,EnvironmentHost host) {
		if(!worksWith(stack, host.getClass())) return null;
		return new LinkingUpgrade((IComputerCart) host);
	}

	@Override
	public String slot(ItemStack stack) {
		return Slot.Upgrade;
	}

	@Override
	public int tier(ItemStack stack) {
		return 0;
	}

	@Override
	public NBTTagCompound dataTag(ItemStack stack) {
		return CustomDriver.dataTag(stack);
	}

}
