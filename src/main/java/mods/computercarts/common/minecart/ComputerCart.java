package mods.computercarts.common.minecart;

import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.internal.Tiered;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.EnvironmentHost;

public interface ComputerCart extends EnvironmentHost, Environment, Agent, Tiered {
    int componentCount();

    Environment getComponentInSlot(int index);
}
