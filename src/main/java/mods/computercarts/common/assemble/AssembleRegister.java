package mods.computercarts.common.assemble;

import li.cil.oc.api.IMC;
import mods.computercarts.common.minecart.EntityComputerCart;

public class AssembleRegister {
    public static void register() {
        IMC.registerAssemblerTemplate("Computer Cart Tier 1",
                "mods.computercarts.common.assemble.ComputerCartT1Template.select",
                "mods.computercarts.common.assemble.ComputerCartT1Template.validate",
                "mods.computercarts.common.assemble.ComputerCartT1Template.assemble",
                EntityComputerCart.class, ComputerCartT1Template.getContainerTier(), ComputerCartT1Template.getUpgradeTier(), ComputerCartT1Template.getComponentSlots());

        IMC.registerAssemblerTemplate("Computer Cart Tier 2",
                "mods.computercarts.common.assemble.ComputerCartT2Template.select",
                "mods.computercarts.common.assemble.ComputerCartT2Template.validate",
                "mods.computercarts.common.assemble.ComputerCartT2Template.assemble",
                EntityComputerCart.class, ComputerCartT2Template.getContainerTier(), ComputerCartT2Template.getUpgradeTier(), ComputerCartT2Template.getComponentSlots());

        IMC.registerAssemblerTemplate("Computer Cart Tier 3",
                "mods.computercarts.common.assemble.ComputerCartT3Template.select",
                "mods.computercarts.common.assemble.ComputerCartT3Template.validate",
                "mods.computercarts.common.assemble.ComputerCartT3Template.assemble",
                EntityComputerCart.class, ComputerCartT3Template.getContainerTier(), ComputerCartT3Template.getUpgradeTier(), ComputerCartT3Template.getComponentSlots());

        IMC.registerAssemblerTemplate("Computer Cart Creative",
                "mods.computercarts.common.assemble.ComputerCartT4Template.select",
                "mods.computercarts.common.assemble.ComputerCartT4Template.validate",
                "mods.computercarts.common.assemble.ComputerCartT4Template.assemble",
                EntityComputerCart.class, ComputerCartT4Template.getContainerTier(), ComputerCartT4Template.getUpgradeTier(), ComputerCartT4Template.getComponentSlots());
    }
}
