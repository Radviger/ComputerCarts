package mods.computercarts.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiUtil {

    //Render Tooltips
    public static void drawHoverText(List<String> text, int x, int y, int width, int height, int guiLeft, FontRenderer font) {
        if (!text.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            int textWidth = -1;
            for (String s : text) {
                if (font.getStringWidth(s) > textWidth)
                    textWidth = font.getStringWidth(s);
            }

            int posX = x + 12;
            int posY = y - 12;
            int textHeight = 8;

            if (text.size() > 1) {
                textHeight += 2 + (text.size() - 1) * 10;
            }
            if (posX + textWidth > width - guiLeft) {

                posX -= 28 + textWidth;
            }
            if (posY + textHeight + 6 > height) {
                posY = height - textHeight - 6;
            }

            int zLevel = 300;
            int bg = 0xF0100010;
            drawGradientRect(posX - 3, posY - 4, posX + textWidth + 3, posY - 3, zLevel, bg, bg);
            drawGradientRect(posX - 3, posY + textHeight + 3, posX + textWidth + 3, posY + textHeight + 4, zLevel, bg, bg);
            drawGradientRect(posX - 3, posY - 3, posX + textWidth + 3, posY + textHeight + 3, zLevel, bg, bg);
            drawGradientRect(posX - 4, posY - 3, posX - 3, posY + textHeight + 3, zLevel, bg, bg);
            drawGradientRect(posX + textWidth + 3, posY - 3, posX + textWidth + 4, posY + textHeight + 3, zLevel, bg, bg);
            int color1 = 0x505000FF;
            int color2 = 0x505000FE;
            drawGradientRect(posX - 3, posY - 3 + 1, posX - 3 + 1, posY + textHeight + 3 - 1, zLevel, color1, color2);
            drawGradientRect(posX + textWidth + 2, posY - 3 + 1, posX + textWidth + 3, posY + textHeight + 3 - 1, zLevel, color1, color2);
            drawGradientRect(posX - 3, posY - 3, posX + textWidth + 3, posY - 3 + 1, zLevel, color1, color1);
            drawGradientRect(posX - 3, posY + textHeight + 2, posX + textWidth + 3, posY + textHeight + 3, zLevel, color2, color2);

            for (int i = 0; i < text.size(); i += 1) {
                font.drawStringWithShadow(text.get(i), posX, posY, -1);
                if (i == 0) {
                    posY += 2;
                }
                posY += 10;
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    public static void drawGradientRect(int posX, int posY, int width, int height, int zLevel, int start, int end) {
        float sa = (float) (start >> 24 & 255) / 255F;
        float sr = (float) (start >> 16 & 255) / 255F;
        float sg = (float) (start >> 8 & 255) / 255F;
        float sb = (float) (start & 255) / 255F;
        float ea = (float) (end >> 24 & 255) / 255F;
        float er = (float) (end >> 16 & 255) / 255F;
        float eg = (float) (end >> 8 & 255) / 255F;
        float eb = (float) (end & 255) / 255F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos((double) width, (double) posY, (double) zLevel).color(sr, sg, sb, sa).endVertex();
        buf.pos((double) posX, (double) posY, (double) zLevel).color(sr, sg, sb, sa).endVertex();
        buf.pos((double) posX, (double) height, (double) zLevel).color(er, eg, eb, ea).endVertex();
        buf.pos((double) width, (double) height, (double) zLevel).color(er, eg, eb, ea).endVertex();
        tes.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
