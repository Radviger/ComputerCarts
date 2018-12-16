package mods.computercarts.common.container;

import mods.computercarts.common.container.slots.SlotGhost;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
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

public class NetworkRailBaseContainer extends Container {

    private TileEntityNetworkRailBase entity;
    private int oldMode;

    public NetworkRailBaseContainer(InventoryPlayer inventory, TileEntityNetworkRailBase entity) {
        this.entity = entity;

        this.addSlotToContainer(new SlotGhost(entity, 0, 116, 35));
        this.addPlayerInv(8, 84, inventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return entity.isUsableByPlayer(player);
    }

    private void addPlayerInv(int x, int y, InventoryPlayer inventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.entity.getMode());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.entity.getMode() != this.oldMode) {
                listener.sendWindowProperty(this, 0, this.entity.getMode());
            }
        }

        this.oldMode = this.entity.getMode();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int field, int value) {
        switch (field) {
            case 0:
                this.entity.setMode(value);
                break;
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        Slot s = this.getSlot(slot);
        if (s == null || !s.getHasStack() || s.inventory.equals(entity)) return ItemStack.EMPTY;
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

    public TileEntityNetworkRailBase getTile() {
        return entity;
    }

}
