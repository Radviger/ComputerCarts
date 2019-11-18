package mods.computercarts.client.gui;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.container.ContainerNetworkRailController;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController.ConnectionMode;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.MessageNetworkMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

public class GuiNetworkRailController extends GuiContainer {

    public static final ResourceLocation texture = new ResourceLocation(ComputerCarts.MODID, "textures/gui/network_rail_controller.png");

    private TileEntityNetworkRailController controller;
    private ConnectionMode oldMode;
    private GuiButton modeBt;


    public GuiNetworkRailController(InventoryPlayer inventory, TileEntityNetworkRailController controller) {
        super(new ContainerNetworkRailController(inventory, controller));
        this.controller = controller;

        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.oldMode = this.controller.getMode();

        this.modeBt = new GuiButton(0, this.guiLeft + 8, this.guiTop + 34, 100, 20, this.getModeName());
        this.buttonList.add(this.modeBt);
    }

    private String getModeName() {
        return I18n.translateToLocal(this.controller.getMode().getName());
    }

    @Override
    public void actionPerformed(GuiButton button) {
        ModNetwork.CHANNEL.sendToServer(new MessageNetworkMode(this.controller.getPos(), (byte)button.id));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1F, 1F, 1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);  //Same as drawTexturedModalRect, but allows custom image sizes
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String caption = I18n.translateToLocal(controller.getName());
        String inventory = I18n.translateToLocal("container.inventory");
        String info = I18n.translateToLocal("gui." + ComputerCarts.MODID + ".network_rail_controller.info");

        this.fontRenderer.drawString(caption, this.xSize / 2 - this.fontRenderer.getStringWidth(caption) / 2, 6, 0x404040);
        this.fontRenderer.drawString(inventory, 8, this.ySize - 96 + 2, 0x404040);
        this.fontRenderer.drawString(info, 8, 24, 0x404040);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.controller.getMode() != this.oldMode) {
            this.modeBt.displayString = this.getModeName();
            this.oldMode = this.controller.getMode();
        }
    }
}
