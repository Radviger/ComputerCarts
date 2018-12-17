package mods.computercarts.common.inventory;

import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class ComputerCartInventory extends Inventory {

    private EntityComputerCart cart;

    public ComputerCartInventory(EntityComputerCart cart) {
        this.cart = cart;
    }

    @Override
    public int getSizeInventory() {
        return cart.getInventorySpace();
    }

    @Override
    public boolean isEmpty() {
        return cart.isEmpty();
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return cart.removeStackFromSlot(slot);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.cart.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        cart.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        cart.closeInventory(player);
    }

    @Override
    public int getField(int field) {
        return cart.getField(field);
    }

    @Override
    public void setField(int field, int value) {
        cart.setField(field, value);
    }

    @Override
    public int getFieldCount() {
        return cart.getFieldCount();
    }

    @Override
    public void clear() {
        cart.clear();
    }

    @Override
    protected void slotChanged(int slot) {
        if (!this.cart.world.isRemote) {
            this.cart.machine().signal("inventory_changed", slot);
        }
    }

    public Iterable<ItemStack> removeOverflowItems(int size) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = size; i < this.getSizeInventory(); i += 1) {
            if (!this.getStackInSlot(i).isEmpty()) {
                list.add(this.getStackInSlot(i));
                this.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
        return list;
    }

    @Override
    public int getMaxSizeInventory() {
        return 80;
    }

    @Override
    public String getName() {
        return cart.getName();
    }

    @Override
    public boolean hasCustomName() {
        return cart.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return cart.getDisplayName();
    }
}
