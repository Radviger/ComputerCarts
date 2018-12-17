package mods.computercarts.common.minecart;

import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.internal.Tiered;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.EnvironmentHost;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface ComputerCart extends EnvironmentHost, Environment, Agent, Tiered, IInventory {

    //Get Number of Components
    int componentCount();

    //Returns Component in Slot
    Environment getComponentInSlot(int index);

    //Synchronize Component Slot with all near Players
    void synchronizeComponentSlot(int slot);

}
