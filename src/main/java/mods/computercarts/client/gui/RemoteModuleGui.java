package mods.computercarts.client.gui;

import mods.computercarts.ComputerCarts;
import mods.computercarts.client.gui.widget.GuiUtil;
import mods.computercarts.common.container.RemoteModuleContainer;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.GuiButtonClick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteModuleGui extends GuiContainer {
    private GuiTextField pass;
    private ResourceLocation texture = new ResourceLocation(ComputerCarts.MODID + ":textures/gui/remotemodulegui.png");

    private int passMsgR = 100;
    private boolean oPerm = false;
    private boolean locked = false;

    public RemoteModuleGui() {
        super(new RemoteModuleContainer());

        this.xSize = 150;
        this.ySize = 117;
    }

    @Override
    public void initGui() {
        super.initGui();

        pass = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 8, 85, 75, 10);
        pass.setFocused(false);
        pass.setEnabled(false);
        pass.setMaxStringLength(10);

        this.buttonList.add(new GuiButton(0, 85 + this.guiLeft, 84 + this.guiTop, 60, 20, I18n.translateToLocal("gui." + ComputerCarts.MODID + ".general.confirm")));
        this.buttonList.add(new GuiButton(1, 126 + this.guiLeft, 4 + this.guiTop, 20, 20, "U"));
        this.buttonList.add(new GuiButton(2, 8 + this.guiLeft, 20 + this.guiTop, 84, 20, TextFormatting.RED + I18n.translateToLocal("gui." + ComputerCarts.MODID + ".remotem.remmodule")));

        this.buttonList.get(0).enabled = false;
        this.buttonList.get(1).enabled = false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1F, 1F, 1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);  //Same as drawTexturedModalRect, but allows custom image sizes
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        this.fontRenderer.drawString(I18n.translateToLocal("gui." + ComputerCarts.MODID + ".remmconf"), 8, 8, 0x404040);
        pass.drawTextBox();
        int stat = this.getContainer().passstate;
        if (stat == 1)
            this.fontRenderer.drawString(I18n.translateToLocal("gui." + ComputerCarts.MODID + ".general.success"), 10, 100, 0x10AA10);
        else if (stat == 2)
            this.fontRenderer.drawString(I18n.translateToLocal("gui." + ComputerCarts.MODID + ".general.failed"), 10, 100, 0xFF3030);

        this.fontRenderer.drawString(I18n.translateToLocal("gui." + ComputerCarts.MODID + ".remotem.chpass"), 8, 73, 0x404040);

        GuiButton b1 = this.buttonList.get(0);
        if (this.isPointInRegion(b1.x, b1.y, b1.width, b1.height, mx + this.guiLeft, my + this.guiTop)) {
            List<String> txt = new ArrayList<>();
            txt.add(TextFormatting.WHITE + I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.chpass"));
            txt.add(TextFormatting.GRAY + I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.empass"));
            GuiUtil.drawHoverText(txt, mx - this.guiLeft, my - this.guiTop, this.width, this.height, this.guiLeft, Minecraft.getMinecraft().fontRenderer);
        }

        b1 = this.buttonList.get(1);
        if (this.isPointInRegion(b1.x, b1.y, b1.width, b1.height, mx + this.guiLeft, my + this.guiTop)) {
            List<String> txt = new ArrayList<>();
            txt.add(TextFormatting.WHITE + ((locked) ? "Locked" : "Unlocked"));
            txt.add(TextFormatting.GRAY + I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.lockbtn"));
            GuiUtil.drawHoverText(txt, mx - this.guiLeft, my - this.guiTop, this.width, this.height, this.guiLeft, Minecraft.getMinecraft().fontRenderer);
        }

        GL11.glPopAttrib();
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) super.keyTyped(ch, key);
        else if (pass.isFocused()) pass.textboxKeyTyped(ch, key);
        else super.keyTyped(ch, key);
    }

    @Override
    protected void mouseClicked(int x, int y, int key) throws IOException {
        super.mouseClicked(x, y, key);

        pass.mouseClicked(x - this.guiLeft, y - this.guiTop, key);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("password", pass.getText());
                ModNetwork.CHANNEL.sendToServer(new GuiButtonClick(2, 0, nbt));
                this.pass.setText("");
                break;
            case 1:
                ModNetwork.CHANNEL.sendToServer(new GuiButtonClick(2, 1, null));
                break;
            case 2:
                ModNetwork.CHANNEL.sendToServer(new GuiButtonClick(2, 2, null));
                break;
        }
    }

    private RemoteModuleContainer getContainer() {
        return (RemoteModuleContainer) this.inventorySlots;
    }

    @Override
    public void updateScreen() {
        if (this.getContainer().passstate != 0) {
            this.passMsgR--;
            if (this.passMsgR <= 0) this.getContainer().passstate = 0;
        } else if (this.passMsgR != 100) this.passMsgR = 100;

        if (locked != this.getContainer().locked) {
            locked = this.getContainer().locked;
            this.buttonList.get(1).displayString = (locked) ? "L" : "U";
        }

        if (oPerm != this.getContainer().perm) {
            oPerm = this.getContainer().perm;
            this.pass.setEnabled(oPerm);
            this.buttonList.get(0).enabled = oPerm;
            this.buttonList.get(1).enabled = oPerm;
        }
    }
}
