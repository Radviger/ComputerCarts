package mods.computercarts.client;

import li.cil.oc.api.Manual;
import li.cil.oc.api.prefab.ItemStackTabIconRenderer;
import li.cil.oc.api.prefab.ResourceContentProvider;
import mods.computercarts.ComputerCarts;
import mods.computercarts.client.manual.ManualPathProvider;
import mods.computercarts.client.renderer.entity.RenderComputerCart;
import mods.computercarts.client.renderer.item.CartItemRenderer;
import mods.computercarts.common.CommonProxy;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        RenderingRegistry.registerEntityRenderingHandler(EntityComputerCart.class, RenderComputerCart::new);
    }

    @Override
    public void init() {
        super.init();
        Manual.addProvider(new ResourceContentProvider(ComputerCarts.MODID, "doc/"));
        Manual.addProvider(new ManualPathProvider());
        Manual.addTab(new ItemStackTabIconRenderer(new ItemStack(ModItems.COMPUTER_CART_CASE, 1, 0)), "gui." + ComputerCarts.MODID + ".manual", ComputerCarts.MODID + "/%LANGUAGE%/index.md");
    }

    @Override
    public void postInit() {
        super.postInit();
        TileEntityItemStackRenderer.instance = new CartItemRenderer(TileEntityItemStackRenderer.instance);
    }
}
