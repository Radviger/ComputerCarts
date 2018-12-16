package mods.computercarts.common.disassemble;

import li.cil.oc.api.IMC;
import mods.computercarts.ComputerCarts;


public class DisassembleRegister {
    public static void register() {
        IMC.registerDisassemblerTemplate("Computer Cart",
                "mods.computercarts.common.disassemble.ComputerCartTemplate.select",
                "mods.computercarts.common.disassemble.ComputerCartTemplate.disassemble");

        IMC.registerDisassemblerTemplate(ComputerCarts.NAME + "-Standard Template",
                "mods.computercarts.common.disassemble.StandardTemplate.select",
                "mods.computercarts.common.disassemble.StandardTemplate.disassemble");
    }
}
