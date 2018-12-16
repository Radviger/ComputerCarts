package mods.computercarts.client.renderer.item;

import mods.computercarts.ComputerCarts;
import mods.computercarts.client.renderer.entity.ComputerCartModel;
import mods.computercarts.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CartItemRenderer extends TileEntityItemStackRenderer {

    private final ResourceLocation texture = new ResourceLocation(ComputerCarts.MODID + ":textures/entity/computercart.png");
    private final ComputerCartModel model = new ComputerCartModel();
    private final TileEntityItemStackRenderer wrapped;

    public CartItemRenderer(TileEntityItemStackRenderer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void renderByItem(ItemStack item) {
        if (item.getItem() != ModItems.COMPUTER_CART) {
            this.wrapped.renderByItem(item);
        } else {
            GL11.glPushMatrix();
            GL11.glRotatef(130.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.7F, 0F, 0F);
            GL11.glScaled(0.7, 0.7, 0.7);
            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            model.renderItem(0.0625F);
            GL11.glPopMatrix();
        }
    }
}
