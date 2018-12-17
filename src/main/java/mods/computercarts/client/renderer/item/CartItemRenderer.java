package mods.computercarts.client.renderer.item;

import mods.computercarts.ComputerCarts;
import mods.computercarts.client.renderer.entity.ModelComputerCart;
import mods.computercarts.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CartItemRenderer extends TileEntityItemStackRenderer {

    private final ResourceLocation texture = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart.png");
    private final ModelComputerCart model = new ModelComputerCart();
    private final TileEntityItemStackRenderer wrapped;

    public CartItemRenderer(TileEntityItemStackRenderer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void renderByItem(ItemStack item) {
        if (item.getItem() != ModItems.COMPUTER_CART) {
            this.wrapped.renderByItem(item);
        } else {
            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            GlStateManager.scale(-1F, -1F, 1F);
            model.renderItem(0.625F);
            GlStateManager.popMatrix();
        }
    }
}
