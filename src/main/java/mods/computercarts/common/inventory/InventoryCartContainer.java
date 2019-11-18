package mods.computercarts.common.inventory;

import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Inventory;
import mods.computercarts.ComputerCarts;
import mods.computercarts.common.driver.CustomDriver;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.util.ItemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class InventoryCartContainer implements IInventory {

    private EntityComputerCart cart;

    private NonNullList<ItemStack> items = NonNullList.withSize(this.getMaxSizeInventory(), ItemStack.EMPTY);
    private int selectedSlot;
    private int size = 0;

    public InventoryCartContainer(EntityComputerCart cart) {
        this.cart = cart;
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        if (slot < this.getMaxSizeInventory()) {
            ItemStack result = items.get(slot);
            this.setInventorySlotContents(slot, ItemStack.EMPTY);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(this.cart) <= 64 && !this.cart.isDead;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public int getField(int field) {
        return 0;
    }

    @Override
    public void setField(int field, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        items.clear();
    }

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

    public int getMaxSizeInventory() {
        return 80;
    }

    @Override
    public String getName() {
        return "inventory." + ComputerCarts.MODID + ".computercart";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return slot < this.getMaxSizeInventory() ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int slot, int number) {
        if (slot >= 0 && slot < this.getMaxSizeInventory()) {
            if (number >= items.get(slot).getCount()) {
                ItemStack get = items.get(slot);
                items.set(slot, ItemStack.EMPTY);
                this.slotChanged(slot);
                return get;
            } else {
                ItemStack ret = items.get(slot).splitStack(number);
                if (items.get(slot).isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }
                this.slotChanged(slot);
                return ret;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        if (slot < this.getMaxSizeInventory()) {
            items.set(slot, stack);
            this.slotChanged(slot);
        }
    }

    //This is the same as setInventorySlotContents but will not send a Signal to the machine;
    private void updateSlotContents(int slot, @Nonnull ItemStack stack) {
        if (slot < this.getMaxSizeInventory()) {
            items.set(slot, stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        for (int i = 0; i < this.getSizeInventory(); i += 1) {
            ItemStack stack = this.getStackInSlot(i);
            if (!stack.isEmpty()) {
                this.updateSlotContents(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound output = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        for (byte i = 0; i < this.getMaxSizeInventory(); i += 1) {
            NBTTagCompound slot = new NBTTagCompound();
            ItemStack stack = this.getStackInSlot(i);
            slot.setByte("slot", i);
            if (!stack.isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                stack.writeToNBT(item);
                slot.setTag("item", item);
            }
            items.appendTag(slot);
        }
        output.setTag("items", items);
        output.setInteger("selectedSlot", this.selectedSlot);
        return output;
    }

    public void readFromNBT(NBTTagCompound input) {
        NBTTagList items = (NBTTagList) input.getTag("items");
        int tagCount = items.tagCount();
        for (int i = 0; i < tagCount; i += 1) {
            NBTTagCompound slot = items.getCompoundTagAt(i);
            if (slot.hasKey("item")) {
                this.updateSlotContents(slot.getByte("slot"), new ItemStack(slot.getCompoundTag("item")));
            }
        }
        this.selectedSlot = input.getInteger("selectedSlot");
    }

    public void recalculateSize() {
        EntityComputerCart cart = this.cart;
        InventoryCartComponents components = cart.componentInventory;

        for (int i = 0; i < components.getSizeInventory(); i += 1) {
            if (!components.getStackInSlot(i).isEmpty()) {
                ItemStack stack = components.getStackInSlot(i);
                DriverItem drv = CustomDriver.driverFor(stack, cart.getClass());
                if (drv instanceof Inventory && this.size < this.getMaxSizeInventory()) {
                    this.size = this.size + ((Inventory) drv).inventoryCapacity(stack);
                    if (this.size > this.getMaxSizeInventory())
                        this.size = this.getMaxSizeInventory();
                }
            }
        }
        Iterable<ItemStack> over = this.removeOverflowItems(this.size);
        ItemUtil.dropItems(over, cart.world, cart.posX, cart.posY, cart.posZ, true);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }
}
