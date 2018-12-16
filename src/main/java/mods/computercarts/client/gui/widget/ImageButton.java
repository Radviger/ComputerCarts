package mods.computercarts.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class ImageButton extends GuiButton {

    private boolean isToggleButton;
    private boolean toggle;
    private ResourceLocation texture;

    public ImageButton(int id, int posx, int posy, int width, int height, String text, ResourceLocation texture, boolean isToggleButton) {
        super(id, posx, posy, width, height, text);
        this.isToggleButton = isToggleButton;
        this.texture = texture;
        this.toggle = false;
    }

    public ImageButton(int id, int posx, int posy, int width, int height, ResourceLocation texture, String text) {
        super(id, posx, posy, width, height, text);
        this.isToggleButton = false;
        this.texture = texture;
        this.toggle = false;
    }

    @Override
    public void drawButton(Minecraft client, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            client.renderEngine.bindTexture(this.texture);

            boolean hover = mouseX <= this.x + this.width && mouseX >= this.x && mouseY <= this.y + this.height && mouseY >= this.y;

            Tessellator tes = Tessellator.getInstance();
            BufferBuilder buf = tes.getBuffer();

            double v0 = hover && this.enabled ? 0.5 : 0;
            double v1 = v0 + 0.5;
            double u0 = this.toggle ? 0.5 : 0;
            double u1 = u0 + (this.isToggleButton ? 0.5 : 1);

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(this.x, this.y + this.height, this.zLevel).tex(u0, v1).endVertex();
            buf.pos(this.x + this.width, this.y + this.height, this.zLevel).tex(u1, v1).endVertex();
            buf.pos(this.x + this.width, this.y, this.zLevel).tex(u1, v0).endVertex();
            buf.pos(this.x, this.y, this.zLevel).tex(u0, v0).endVertex();
            tes.draw();

            if (this.displayString != null && !Objects.equals(this.displayString, "")) {
                int color = !this.enabled ? 0xA0A0A0 : hover ? 0xFFFFA0 : 0xE0E0E0;
                this.drawCenteredString(client.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
            }
        }
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public boolean getToggle() {
        return this.toggle;
    }


}
