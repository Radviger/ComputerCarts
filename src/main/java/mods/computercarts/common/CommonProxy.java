package mods.computercarts.common;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.assemble.AssembleRegister;
import mods.computercarts.common.blocks.ModBlocks;
import mods.computercarts.common.disassemble.DisassembleRegister;
import mods.computercarts.common.driver.CustomDriver;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.recipe.Recipes;
import mods.computercarts.interaction.railcraft.RailcraftCompat;
import mods.computercarts.interaction.waila.WailaCompat;
import mods.computercarts.network.ModNetwork;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy {


    public void postInit() {
    }

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ComputerCarts.MODID, new GuiHandler());

        DisassembleRegister.register();
        AssembleRegister.register();
        CustomDriver.init();
        Recipes.init();
        RemoteExtenderRegister.register();

        if (Loader.isModLoaded("Waila")) WailaCompat.init();
        if (Loader.isModLoaded("Railcraft")) RailcraftCompat.init();
    }

    public void preInit() {
        EventHandler.initHandler();

        ModNetwork.init();
        ModItems.init();
        ModBlocks.init();

        EntityRegistry.registerModEntity(new ResourceLocation(ComputerCarts.MODID, "computer_cart"), EntityComputerCart.class, "computer_cart", 1, ComputerCarts.MODID, 80, 1, true);
    }

}
