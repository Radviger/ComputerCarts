package mods.computercarts.common.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public abstract class Inventory implements IInventory {

    private NonNullList<ItemStack> stacks = NonNullList.withSize(this.getMaxSizeInventory(), ItemStack.EMPTY);

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return slot < this.getMaxSizeInventory() ? this.stacks.get(slot) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int slot, int number) {
        if (slot >= 0 && slot < this.getMaxSizeInventory()) {
            if (number >= stacks.get(slot).getCount()) {
                ItemStack get = stacks.get(slot);
                stacks.set(slot, ItemStack.EMPTY);
                this.slotChanged(slot);
                return get;
            } else {
                ItemStack ret = stacks.get(slot).splitStack(number);
                if (stacks.get(slot).isEmpty()) {
                    stacks.set(slot, ItemStack.EMPTY);
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
            stacks.set(slot, stack);
            this.slotChanged(slot);
        }
    }

    //This is the same as setInventorySlotContents but will not send a Signal to the machine;
    private void updateSlotContents(int slot, @Nonnull ItemStack stack) {
        if (slot < this.getMaxSizeInventory()) {
            stacks.set(slot, stack);
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

    public NBTTagList writeToNBT() {
        NBTTagList tag = new NBTTagList();
        for (byte i = 0; i < this.getMaxSizeInventory(); i += 1) {
            NBTTagCompound slot = new NBTTagCompound();
            ItemStack stack = this.getStackInSlot(i);
            slot.setByte("slot", i);
            if (!stack.isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                stack.writeToNBT(item);
                slot.setTag("item", item);
            }
            tag.appendTag(slot);
        }
        return tag;
    }

    public void readFromNBT(NBTTagList tag) {
        int tagCount = tag.tagCount();
        for (int i = 0; i < tagCount; i += 1) {
            NBTTagCompound slot = tag.getCompoundTagAt(i);
            if (slot.hasKey("item")) {
                this.updateSlotContents(slot.getByte("slot"), new ItemStack(slot.getCompoundTag("item")));
            }
        }
    }

    protected abstract void slotChanged(int slot);

    abstract public int getMaxSizeInventory();

}
