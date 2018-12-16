package mods.computercarts;

import mods.computercarts.common.CommonProxy;
import mods.computercarts.common.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ComputerCarts.MODID, name = ComputerCarts.NAME, version = ComputerCarts.VERSION, dependencies = "required-after:opencomputers@[1.7.3,);"
        + "after:waila;"
        + "after:railcraft@[9.7.0.0,)")
public class ComputerCarts {
    public static final String MODID = "computercarts";
    public static final String VERSION = "{@mod:version}";
    public static final String NAME = "ComputerCarts";

    @Mod.Instance(ComputerCarts.MODID)
    public static ComputerCarts INSTANCE;

    public static Logger LOGGER = LogManager.getLogger(ComputerCarts.NAME);

    @SidedProxy(serverSide = "mods.computercarts.common.CommonProxy", clientSide = "mods.computercarts.client.ClientProxy")
    public static CommonProxy PROXY;

    public static Configuration CONFIG;

    public static CreativeTabs TAB = new CreativeTabs(ComputerCarts.MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModItems.COMPUTER_CART_CASE);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CONFIG = new Configuration(event.getSuggestedConfigurationFile());
        Settings.init();

        PROXY.preInit();
    }

    @Mod.EventHandler
    public void Init(FMLInitializationEvent event) {
        logModApis();

        PROXY.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit();
    }

    private void logModApis() {
        if (Loader.isModLoaded("appliedenergistics2")) ComputerCarts.LOGGER.info("Found Mod: AE2");
        if (Loader.isModLoaded("jei")) ComputerCarts.LOGGER.info("Found Mod: JEI");
        if (Loader.isModLoaded("Waila")) ComputerCarts.LOGGER.info("Found Mod: WAILA");
        if (Loader.isModLoaded("Railcraft")) ComputerCarts.LOGGER.info("Found Mod: Railcraft");
    }
}
