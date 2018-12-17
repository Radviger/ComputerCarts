package mods.computercarts.client.gui.widget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public abstract class SlotComponent extends Slot {

    protected EntityPlayer player;
    protected Container container;
    protected String slot;
    protected int tier;

    public SlotComponent(IInventory inventory, int id, int x, int y, EntityPlayer player, Container container, int tier, String type) {
        super(inventory, id, x, y);
        this.container = container;
        this.player = player;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isEnabled() {
        return !Objects.equals(slot, li.cil.oc.api.driver.item.Slot.None) && super.isEnabled() && tier != -1;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.inventory.isItemValidForSlot(this.slotNumber, stack);
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        ItemStack result = super.onTake(player, stack);
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof SlotComponent) {
                ((SlotComponent) slot).clearIfInvalid(player);
            }
        }
        return result;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof SlotComponent) {
                ((SlotComponent) slot).clearIfInvalid(player);
            }
        }
    }

    protected abstract void clearIfInvalid(EntityPlayer player);
}
