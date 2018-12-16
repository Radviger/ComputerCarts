package mods.computercarts.client.renderer.entity;

import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ComputerCartModel extends ModelBase {
    ModelRenderer Shape1;
    ModelRenderer light;
    ModelRenderer Shape3;

    public ComputerCartModel() {
        textureWidth = 256;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 16, 7, 20);
        Shape1.setRotationPoint(-8F, -1F, -10F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        light = new ModelRenderer(this, 72, 0);
        light.addBox(0F, 0F, 0F, 14, 1, 18);
        light.setRotationPoint(-7F, -2F, -9F);
        light.setTextureSize(64, 32);
        light.mirror = true;
        Shape3 = new ModelRenderer(this, 0, 27);
        Shape3.addBox(0F, 0F, 0F, 16, 4, 20);
        Shape3.setRotationPoint(-8F, -6F, -10F);
        Shape3.setTextureSize(64, 32);
        Shape3.mirror = true;
    }


    public void renderTile(Entity entity, float f) {
        Shape1.render(f);
        Shape3.render(f);
        if (((EntityComputerCart) entity).getRunning()) {
            EntityComputerCart cart = (EntityComputerCart) entity;
            byte[] rgb = ColorUtil.colorToRGB(cart.getLightColor());
            GlStateManager.disableLighting();
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
            GlStateManager.enableBlend();
            GlStateManager.color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
        } else {
            GL11.glColor3d(0, 0, 0);
        }
        light.render(f);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        GlStateManager.color(1, 1, 1);
    }

    public void renderItem(float f) {
        Shape1.render(f);
        Shape3.render(f);
        GlStateManager.color(0, 0, 0);
        light.render(f);
        GlStateManager.color(1, 1, 1);
    }
}
