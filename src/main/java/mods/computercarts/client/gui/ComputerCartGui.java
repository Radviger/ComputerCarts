package mods.computercarts.client.gui;

import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.client.KeyBindings;
import li.cil.oc.client.renderer.TextBufferRenderCache;
import li.cil.oc.client.renderer.gui.BufferRenderer;
import li.cil.oc.integration.jei.ModJEI;
import li.cil.oc.integration.util.ItemSearch$;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.IJeiRuntime;
import mods.computercarts.ComputerCarts;
import mods.computercarts.Settings;
import mods.computercarts.client.SlotIcons;
import mods.computercarts.client.gui.widget.*;
import mods.computercarts.common.container.ComputerCartContainer;
import mods.computercarts.common.container.slots.ContainerSlot;
import mods.computercarts.common.inventory.ComponentInventory;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.GuiButtonClick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ComputerCartGui extends GuiContainer {

    //Resources
    private ResourceLocation textureNoScreen = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/robot_noscreen.png");
    private ResourceLocation textureScreen = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/robot.png");
    private ResourceLocation textureOnOffButton = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/button_power.png");
    private ResourceLocation ebar = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/bar.png");
    private ResourceLocation selection = new ResourceLocation(Settings.OC_ResLoc, "textures/gui/robot_selection.png");

    //Container (as INSTANCE of ComputerCartContainer)
    private ComputerCartContainer container;

    //Textbuffer and keyboard
    private int txtWidth, txtHeight;

    private double maxBufferWidth = 240.0;
    private double maxBufferHeight = 140.0;
    private double bufferscale = 0.0;
    private double bufferRenderWidth = Math.min(maxBufferWidth, TextBufferRenderCache.renderer().charRenderWidth() * 50);
    private double bufferRenderHeight = Math.min(maxBufferHeight, TextBufferRenderCache.renderer().charRenderHeight() * 16);
    private int bufferX = (int) (8 + (this.maxBufferWidth - this.bufferRenderWidth) / 2);
    private int bufferY = (int) (8 + (this.maxBufferHeight - this.bufferRenderHeight) / 2);
    private TextBuffer textbuffer;
    private boolean hasKeyboard = false;
    private Map<Integer, Character> pressedKeys = new HashMap<>();

    //Other stuff
    private ImageButton btPower;

    private Slot hoveredSlot = null;
    @Nonnull
    private ItemStack hoveredJEI = ItemStack.EMPTY;
    private final SliderButton invslider;

    private boolean[] disSlot = new boolean[16];
    private int offset = 0;

//-------Init functions-------//

    public ComputerCartGui(InventoryPlayer inventory, EntityComputerCart entity) {
        super(new ComputerCartContainer(inventory, entity));
        this.container = (ComputerCartContainer) this.inventorySlots;

        this.initComponents(entity.compinv);

        this.ySize = (container.getHasScreen()) ? ComputerCartContainer.YSIZE_SCR : ComputerCartContainer.YSIZE_NOSCR;
        this.xSize = ComputerCartContainer.XSIZE;
        this.offset = (this.textbuffer != null) ? ComputerCartContainer.DELTA : 0;
        this.invslider = new SliderButton(244, 8 + offset, 6, 13, 94);
    }

    //Initialize components. get Screen and check if there is a Keyboard
    private void initComponents(ComponentInventory compinv) {
        for (ManagedEnvironment component : compinv.getComponents()) {
            if (component instanceof TextBuffer) this.textbuffer = (TextBuffer) component;
            else if (component instanceof li.cil.oc.server.component.Keyboard) this.hasKeyboard = true;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.updateSlots();

        BufferRenderer.init(Minecraft.getMinecraft().renderEngine);
        boolean guiSizeChange = true;

        this.txtHeight = (this.textbuffer != null) ? this.textbuffer.getHeight() : 0;
        this.txtWidth = (this.textbuffer != null) ? this.textbuffer.getWidth() : 0;

        BufferRenderer.compileBackground((int) this.bufferRenderWidth, (int) this.bufferRenderHeight, true);

        this.btPower = new ImageButton(0, this.guiLeft + 5, 5 + this.guiTop + offset, 18, 18, "", textureOnOffButton, true);

        this.buttonList.add(this.btPower);

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

//-------Override render functions-------//

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1F, 1F, 1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture((container.getHasScreen()) ? textureScreen : textureNoScreen);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        this.renderGuiSlots();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        if (this.container.getHasScreen() && this.textbuffer != null) {
            this.drawBufferLayer();

            double bw = this.txtWidth * TextBufferRenderCache.renderer().charRenderWidth();
            double bh = this.txtHeight * TextBufferRenderCache.renderer().charRenderHeight();
            double scaleX = Math.min(this.bufferRenderWidth / bw, 1);
            double scaleY = Math.min(this.bufferRenderHeight / bh, 1);
            this.bufferscale = Math.min(scaleX, scaleY);
        }

        //Widgets
        EnergyBar.drawBar(26, 8 + offset, 12, 140, 150, (double) this.container.sEnergy / (double) this.container.smaxEnergy, ebar);
        this.invslider.drawSlider(this.zLevel, this.invslider.getActive() || this.invslider.isMouseHoverButton(mouseX - this.guiLeft, mouseY - this.guiTop));

        //Highlight
        for (Slot slot : this.container.inventorySlots) this.drawSlotHighlight(slot);

        //Render selected slot
        if (this.container.sizeinv > 0)
            this.drawSelection();

        //Tooltips
        if (this.isPointInRegion(this.btPower.x, this.btPower.y, 18, 18, mouseX + this.guiLeft, mouseY + this.guiTop)) {
            List<String> ls = new ArrayList<>();
            if (this.container.getEntity().getRunning()) {
                ls.add(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.turnoff"));
            } else {
                ls.add(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.turnon"));
                ls.add(TextFormatting.GRAY + I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".gui.useanalyzer"));
            }
            GuiUtil.drawHoverText(ls, mouseX - this.guiLeft, mouseY - this.guiTop, this.width, this.height, this.guiLeft, Minecraft.getMinecraft().fontRenderer);
        }
        if (this.isPointInRegion(26 + this.guiLeft, 8 + this.guiTop + offset, 140, 12, mouseX + this.guiLeft, mouseY + this.guiTop)) {
            List<String> ls = new ArrayList<>();
            int per = (int) (((double) this.container.sEnergy / (double) this.container.smaxEnergy) * 100);
            ls.add("Energy: " + per + "% (" + this.container.sEnergy + " / " + this.container.smaxEnergy + ")");
            GuiUtil.drawHoverText(ls, mouseX - this.guiLeft, mouseY - this.guiTop, this.width, this.height, this.guiLeft, Minecraft.getMinecraft().fontRenderer);
        }

        GL11.glPopAttrib();
    }

    public void drawScreen(int mx, int my, float dt) {
        this.hoveredSlot = null;
        for (Slot slot : this.container.inventorySlots) {
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

//-------Events-------//

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                ModNetwork.CHANNEL.sendToServer(GuiButtonClick.entityButtonClick(this.container.getEntity(), 0, 1));
                break;
        }
    }

    public void updateScreen() {
        if (this.container.getEntity().getRunning() != btPower.getToggle())
            btPower.setToggle(this.container.getEntity().getRunning());
        if (this.container.updatesize) {
            this.invslider.scrollTo(0);
            this.invslider.setMaxSteps(this.container.sizeinv / 4 - 4);
            this.container.updatesize = false;
        }
        if (this.invslider.hasUpdate()) {
            this.invslider.doneUpdate();
            this.updateSlots();
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        boolean isMiddleMouseButton = button == 2;
        boolean isBoundMouseButton = KeyBindings.isPastingClipboard();
        if (this.textbuffer != null && (isMiddleMouseButton || isBoundMouseButton)) {
            if (this.hasKeyboard) {
                this.textbuffer.clipboard(GuiScreen.getClipboardString(), null);
            }
        } else if (button == 0) {
            if (this.invslider.isMouseHoverBox(x - this.guiLeft, y - this.guiTop))
                this.invslider.setActive(true);
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == 0 && this.invslider.getActive()) {
            this.invslider.setActive(false);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (Mouse.hasWheel() && Mouse.getEventDWheel() != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth - guiLeft;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1 - guiTop;
            if (isCoordinateOverInventory(mouseX, mouseY) || invslider.isMouseHoverBox(mouseX, mouseY)) {
                if (Math.signum(Mouse.getEventDWheel()) < 0) invslider.scrollDown();
                else invslider.scrollUp();
            }
        }
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long time) {
        super.mouseClickMove(x, y, button, time);
        if (this.invslider.getActive()) {
            this.invslider.scrollMouse(y - this.guiTop);
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        int code = Keyboard.getEventKey();

        if (this.textbuffer != null && code != Keyboard.KEY_ESCAPE && code != Keyboard.KEY_F11) {
            if (this.hasKeyboard) {
                if (Keyboard.getEventKeyState()) {
                    char ch = Keyboard.getEventCharacter();
                    if (!pressedKeys.containsKey(code) || !ignoreRepeat(ch, code)) {
                        this.textbuffer.keyDown(ch, code, null);
                        pressedKeys.put(code, ch);
                    }
                } else {
                    if (pressedKeys.containsKey(code)) {
                        this.textbuffer.keyUp(pressedKeys.remove(code), code, null);
                    }
                }

                if (KeyBindings.isPastingClipboard()) {
                    this.textbuffer.clipboard(GuiScreen.getClipboardString(), null);
                }
            }

        } else {
            super.handleKeyboardInput();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.textbuffer != null) {
            for (Entry<Integer, Character> e : pressedKeys.entrySet()) {
                this.textbuffer.keyUp(e.getValue(), e.getKey(), null);
            }
        }
        if (Loader.isModLoaded("jei")) {
            this.resetJEIHighlight();
        }
        Keyboard.enableRepeatEvents(false);
    }

    private boolean isCoordinateOverInventory(int x, int y) {
        return x >= 170 && x < 240 && y >= 8 + offset && y < 78 + offset;
    }

    private void updateSlots() {
        for (Slot s : this.container.inventorySlots) {
            int index = s.getSlotIndex() - this.invslider.getScroll() * 4;
            if (s.inventory.equals(this.container.getEntity().maininv)) {
                if (index >= 0 && index < 16 && s.getSlotIndex() < this.container.sizeinv) {
                    s.xPos = 170 + (index % 4) * 18;
                    s.yPos = 8 + offset + (index / 4) * 18;
                    this.disSlot[index] = false;
                } else {
                    s.xPos = -10000;
                    s.yPos = -10000;
                    if (index >= 0 && index < 16) this.disSlot[index] = true;
                }
            }
        }
    }

//-------Render functions----------//

    //Fuction from OC. used in handleKeyboardInput()
    private boolean ignoreRepeat(char ch, int code) {
        return code == Keyboard.KEY_LCONTROL ||
                code == Keyboard.KEY_RCONTROL ||
                code == Keyboard.KEY_LMENU ||
                code == Keyboard.KEY_RMENU ||
                code == Keyboard.KEY_LSHIFT ||
                code == Keyboard.KEY_RSHIFT ||
                code == Keyboard.KEY_LMETA ||
                code == Keyboard.KEY_RMETA;
    }

    //Render the Highlight for Components
    protected void drawSlotHighlight(Slot slot) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player.inventory.getItemStack().isEmpty()) {
            boolean highlight = false;
            if (!(slot instanceof ContainerSlot) || (!Objects.equals(((ContainerSlot) slot).getSlotType(), "none") && ((ContainerSlot) slot).getTier() != -1)) {
                boolean inPlayerInv = slot.inventory == player.inventory;
                if (this.hoveredSlot != null) {
                    if (this.hoveredSlot.getHasStack() && (slot instanceof ContainerSlot) && slot.isItemValid(this.hoveredSlot.getStack()))
                        highlight = true;
                    else if (slot.getHasStack() && (this.hoveredSlot instanceof ContainerSlot) && this.hoveredSlot.isItemValid(slot.getStack()))
                        highlight = true;
                } else {
                    if (!this.hoveredJEI.isEmpty() && (slot instanceof ContainerSlot) && slot.isItemValid(this.hoveredJEI)) {
                        highlight = true;
                    }
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
        return slot.inventory != this.container.getPlayer().inventory;
    }

    private boolean isSelectiveSlot(Slot slot) {
        return slot instanceof ComponentSlot;
    }

    //Draw Screen if there is one
    private void drawBufferLayer() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(bufferX, bufferY, 0);
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-3, -3, 0);
        GlStateManager.color(1, 1, 1, 1);
        BufferRenderer.drawBackground();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        double scaleX = bufferRenderWidth / this.textbuffer.renderWidth();
        double scaleY = bufferRenderHeight / this.textbuffer.renderHeight();
        double scale = Math.min(scaleX, scaleY);
        if (scaleX > scale) {
            GlStateManager.translate(this.textbuffer.renderWidth() * (scaleX - scale) / 2, 0, 0);
        } else if (scaleY > scale) {
            GlStateManager.translate(0, this.textbuffer.renderHeight() * (scaleY - scale) / 2, 0);
        }
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.scale(this.bufferscale, this.bufferscale, 1);
        BufferRenderer.drawText(this.textbuffer);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    //Render the Background Icons
    private void renderGuiSlots() {
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        TextureAtlasSprite non = SlotIcons.fromTier(-1);
        if (non != null) {
            this.drawTexturedModalRect(this.guiLeft + 170, this.guiTop + 84 + offset, non, 16, 16);
        }

        //Render the Icons for Container Slots
        for (Slot slot : this.container.inventorySlots) {
            if (slot instanceof ContainerSlot) {
                TextureAtlasSprite typeicon = SlotIcons.fromSlot(((ContainerSlot) slot).getSlotType());
                if (typeicon != null)
                    this.drawTexturedModalRect(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, typeicon, 16, 16);
            }
        }
        if (non != null) {
            //Render Icons for disabled Inventory Slots
            for (int i = 0; i < 4; i += 1) {
                for (int j = 0; j < 4; j += 1) {
                    int xpos = this.guiLeft + 170 + i * 18;
                    int ypos = this.guiTop + 8 + offset + j * 18;
                    if (this.disSlot[i * j]) {
                        this.drawTexturedModalRect(xpos, ypos, non, 16, 16);
                    }
                }
            }
        }
    }

    private void drawSelection() {
        int slot = this.container.selSlot - this.invslider.getScroll() * 4;
        if (slot >= 0 && slot < 16) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderHelper.disableStandardItemLighting();
            Minecraft.getMinecraft().renderEngine.bindTexture(selection);
            double now = System.currentTimeMillis() / 1000.0;
            double offsetV = (int) ((now % 1) * 17) / 17D;
            int x = 168 + (slot % 4) * (18);
            int y = 6 + offset + (slot / 4) * (18);

            //ComputerCarts.LOGGER.info(x+" : "+y);

            Tessellator t = Tessellator.getInstance();
            BufferBuilder b = t.getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            b.pos(x, y, zLevel).tex(0, offsetV).endVertex();
            b.pos(x, y + 20, zLevel).tex(0, offsetV + 1D / 17D).endVertex();
            b.pos(x + 20, y + 20, zLevel).tex(1, offsetV + 1D / 17D).endVertex();
            b.pos(x + 20, y, zLevel).tex(1, offsetV).endVertex();
            t.draw();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableBlend();
        }
    }
}
