package mods.computercarts.client.gui;

import li.cil.oc.integration.jei.ModJEI;
import li.cil.oc.integration.util.ItemSearch$;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.IJeiRuntime;
import mods.computercarts.ComputerCarts;
import mods.computercarts.client.gui.widget.ComponentSlot;
import mods.computercarts.common.container.NetworkRailBaseContainer;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.GuiButtonClick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.stream.Collectors;

public class NetworkRailBaseGui extends GuiContainer {

    public static final ResourceLocation texture = new ResourceLocation(ComputerCarts.MODID, "textures/gui/netrailbasegui.png");

    private EntityPlayer player;
    private TileEntityNetworkRailBase tile;
    private int oldMode;
    private GuiButton modeBt;

    private Slot hoveredSlot = null;
    @Nonnull
    private ItemStack hoveredJEI = ItemStack.EMPTY;


    public NetworkRailBaseGui(InventoryPlayer inventory, TileEntityNetworkRailBase entity) {
        super(new NetworkRailBaseContainer(inventory, entity));
        this.player = inventory.player;

        this.tile = entity;

        this.xSize = 176;
        this.ySize = 166;
    }

    public void initGui() {
        super.initGui();

        this.oldMode = this.tile.getMode();

        this.modeBt = new GuiButton(0, this.guiLeft + 8, this.guiTop + 34, 100, 20, this.getModeButtonTxt());
        this.buttonList.add(this.modeBt);
    }


    private String getModeButtonTxt() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        String buttonTxt;
        switch (this.tile.getMode()) {
            case 0:
                buttonTxt = "Messages + Power";
                break;
            case 1:
                buttonTxt = "Messages";
                break;
            case 2:
                buttonTxt = "Power";
                break;
            case 3:
                buttonTxt = "None";
                break;
            default:
                buttonTxt = TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "ERROR! no Mode";
                player.sendMessage(new TextComponentString(TextFormatting.RED + "This Mode is not valid. Press the Mode Button to fix it"));
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this issue, if this doesn't work, "));
                break;
        }
        return buttonTxt;
    }

    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                ModNetwork.CHANNEL.sendToServer(GuiButtonClick.tileButtonClick(tile, 0, 0));
                break;
        }
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
        String caption = I18n.translateToLocal(tile.getName());
        String inventory = I18n.translateToLocal("container.inventory");
        String info = I18n.translateToLocal("gui." + ComputerCarts.MODID + ".network_rail_base.info");

        this.fontRenderer.drawString(caption, this.xSize / 2 - this.fontRenderer.getStringWidth(caption) / 2, 6, 0x404040);
        this.fontRenderer.drawString(inventory, 8, this.ySize - 96 + 2, 0x404040);
        this.fontRenderer.drawString(info, 8, 24, 0x404040);

        for (Slot slot : this.inventorySlots.inventorySlots) this.drawSlotHighlight(slot);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void updateScreen() {
        super.updateScreen();

        if (this.tile.getMode() != this.oldMode) {
            this.modeBt.displayString = this.getModeButtonTxt();
            this.oldMode = this.tile.getMode();
        }
    }

    public TileEntity getTile() {
        return this.tile;
    }

    @Override
    public void drawScreen(int mx, int my, float dt) {
        this.hoveredSlot = null;
        for (Slot slot : this.inventorySlots.inventorySlots) {
            if (slot != null) {
                if (this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, mx, my))
                    this.hoveredSlot = slot;
            }
        }
        this.hoveredJEI = ItemSearch$.MODULE$.hoveredStack(this, mx, my).orEmpty();

        super.drawScreen(mx, my, dt);

        if (Loader.isModLoaded("jei")) {
            this.drawJEIHighlight();
        }
    }

    protected void drawSlotHighlight(Slot slot) {
        if (!Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty()) {
            boolean highlight = false;
            boolean inPlayerInv = slot.inventory == Minecraft.getMinecraft().player.inventory;
            if (this.hoveredSlot != null) {
                if (this.hoveredSlot.getHasStack() && slot.inventory.equals(this.tile) && this.tile.isItemValidForSlot(slot.getSlotIndex(), this.hoveredSlot.getStack()))
                    highlight = true;
                else if (slot.getHasStack() && this.hoveredSlot.inventory.equals(this.tile) && this.tile.isItemValidForSlot(this.hoveredSlot.getSlotIndex(), slot.getStack()))
                    highlight = true;
            } else {
                if (!this.hoveredJEI.isEmpty() && slot.inventory.equals(this.tile) && this.tile.isItemValidForSlot(slot.getSlotIndex(), this.hoveredJEI)) {
                    highlight = true;
                }
            }

            if (highlight) {
                this.zLevel += 100;
                this.drawGradientRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0x80FFFFFF, 0x80FFFFFF);
                this.zLevel -= 100;
            }
        }
    }

    @Optional.Method(modid = "jei")
    private void drawJEIHighlight() {
        IJeiRuntime runtime = ModJEI.runtime().get();
        IItemListOverlay overlay = runtime.getItemListOverlay();
        if (this.hoveredSlot != null && !isInPlayerInventory(this.hoveredSlot) && isSelectiveSlot(this.hoveredSlot)) {
            overlay.highlightStacks(overlay.getVisibleStacks().stream()
                    .filter(this.hoveredSlot::isItemValid)
                    .collect(Collectors.toList())
            );
            return;
        }
        overlay.highlightStacks(Collections.emptyList());
    }

    @Optional.Method(modid = "jei")
    private void resetJEIHighlight() {
        IJeiRuntime runtime = ModJEI.runtime().get();
        runtime.getItemListOverlay().highlightStacks(Collections.emptyList());
    }

    private boolean isInPlayerInventory(Slot slot) {
        return slot.inventory != this.player.inventory;
    }

    private boolean isSelectiveSlot(Slot slot) {
        return slot instanceof ComponentSlot;
    }

}
