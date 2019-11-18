package mods.computercarts.common.container;

import mods.computercarts.common.container.slots.SlotGhost;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController.ConnectionMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ContainerNetworkRailController extends Container {

    private TileEntityNetworkRailController controller;
    private ConnectionMode oldMode;

    public ContainerNetworkRailController(InventoryPlayer inventory, TileEntityNetworkRailController controller) {
        this.controller = controller;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 84 + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return controller.isUsableByPlayer(player);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.controller.getMode().ordinal());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.controller.getMode() != this.oldMode) {
                listener.sendWindowProperty(this, 0, this.controller.getMode().ordinal());
            }
        }

        this.oldMode = this.controller.getMode();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int field, int value) {
        switch (field) {
            case 0:
                this.controller.setMode(ConnectionMode.values()[value]);
                break;
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        Slot s = this.getSlot(slot);
        if (s == null || !s.getHasStack()) return ItemStack.EMPTY;
        ItemStack nitem = s.getStack().copy();
        nitem.setCount(0);
        this.getSlot(0).putStack(nitem); // Slot 0 is the camo slot
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slot, int button, ClickType clickType, EntityPlayer player) {
        if (slot >= 0 && slot < this.inventorySlots.size()) {
            Slot s = this.getSlot(slot);
            if ((s instanceof SlotGhost) && (button == 0 || button == 1)) {
                if (s.getHasStack() && !player.inventory.getItemStack().isEmpty() &&
                        player.inventory.getItemStack().getItem() != s.getStack().getItem()) {
                    s.decrStackSize(0);
                }
            }
        }
        return super.slotClick(slot, button, clickType, player);
    }
}
