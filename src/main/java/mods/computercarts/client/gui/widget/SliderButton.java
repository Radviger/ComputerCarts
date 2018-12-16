package mods.computercarts.client.gui.widget;

import mods.computercarts.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SliderButton {

    private ResourceLocation texture;
    private int w;
    private int h;
    private int maxh;
    private int posx;
    private int posy;
    private int slidery;
    private boolean activate = false;

    private int maxSteps = 0;
    private int scroll;
    private boolean update = false;

    public SliderButton(int posx, int posy, int w, int h, int maxh) {
        this.texture = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/button_scroll.png");
        this.w = w;
        this.h = h;
        this.maxh = maxh;
        this.posx = posx;
        this.posy = posy;
        this.slidery = 0;
    }

    public void drawSlider(float zLevel, boolean highlight) {

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();

        double v0 = (highlight) ? 0.5 : 0;
        double v1 = v0 + 0.5;

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        RenderHelper.disableStandardItemLighting();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(posx + w, posy + h + slidery, zLevel).tex(1, v1).endVertex();
        buf.pos(posx + w, posy + slidery, zLevel).tex(1, v0).endVertex();
        buf.pos(posx, posy + slidery, zLevel).tex(0, v0).endVertex();
        buf.pos(posx, posy + h + slidery, zLevel).tex(0, v1).endVertex();
        tes.draw();

        RenderHelper.enableStandardItemLighting();
    }

    public boolean isMouseHoverBox(int mx, int my) {
        return mx >= posx - 1 && mx < posx + w + 1 && my >= posy - 1 && my < posy + maxh + 1;
    }

    public boolean isMouseHoverButton(int mx, int my) {
        return mx >= posx - 1 && mx < posx + w + 1 && my >= posy + slidery - 1 && my < posy + slidery + h + 1;
    }

    public void scrollMouse(int my) {
        this.scrollTo((int) Math.round((my - posy + 1 - 6.5) * (maxSteps - 1) / (maxh - 13.0)));
    }

    public void scrollTo(int pos) {
        if (pos < 0) this.scroll = 0;
        else if (pos > this.maxSteps) this.scroll = this.maxSteps;
        else this.scroll = pos;

        this.slidery = this.maxSteps < 1 ? 0 : (this.maxh - this.h - 2) * this.scroll / this.maxSteps;
        this.update = true;
    }

    public int getScroll() {
        return this.scroll;
    }

    public void scrollDown() {
        this.scrollTo(this.scroll + 1);
    }

    public void scrollUp() {
        this.scrollTo(this.scroll - 1);
    }

    /*----------Setter/Getter---------*/

    public void setActive(boolean state) {
        this.activate = state;
    }

    public boolean getActive() {
        return this.activate;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        if (maxSteps < 0) maxSteps = 0;
        this.maxSteps = maxSteps;
    }

    public boolean hasUpdate() {
        return this.update;
    }

    public void doneUpdate() {
        this.update = false;
    }
}
