package mods.computercarts.interaction.railcraft;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import mods.computercarts.common.minecart.EntityAdvancedCart;
import net.minecraftforge.common.MinecraftForge;

public class RailcraftCompat {

    Object2BooleanMap<EntityAdvancedCart> carts = new Object2BooleanOpenHashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new RailcraftCompat());
    }

    /*@SubscribeEvent
    public void onCartLockdown(CartLockdownEvent.Lock event) {
        if (!(event.cart instanceof EntityAdvancedCart)) return;
        if (!carts.containsKey(event.cart) || !carts.get(event.cart)) {
            ((EntityAdvancedCart) event.cart).lockdown(true);
            carts.put((EntityAdvancedCart) event.cart, true);
        }

    }

    @SubscribeEvent
    public void onCartRelease(CartLockdownEvent.Release event) {
        if (!(event.cart instanceof EntityAdvancedCart)) return;
        if (!carts.containsKey(event.cart) || carts.get(event.cart)) {
            ((EntityAdvancedCart) event.cart).lockdown(false);
            carts.put((EntityAdvancedCart) event.cart, false);
        }
    }*/
}
