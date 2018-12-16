package mods.computercarts.interaction.waila;

public class WailaCompat {

    public static void init() {
        //FMLInterModComms.sendMessage("Waila", "register", "mods.computercarts.interaction.waila.WailaCompat.register");
    }

    /*public static void register(IWailaRegistrar registrar) {
        ComputerCartDataProvider ccdp = new ComputerCartDataProvider();

        registrar.registerNBTProvider(ccdp, EntityComputerCart.class);

        registrar.registerBodyProvider(ccdp, EntityComputerCart.class);
        registrar.registerHeadProvider(ccdp, EntityComputerCart.class);
    }*/
}
