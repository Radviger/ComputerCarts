package mods.computercarts.common.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class InventoryUtil {

    public static int dropItemInventoryWorld(@Nonnull ItemStack stack, World world, BlockPos pos, @Nonnull EnumFacing side, int num) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof IInventory) {
            return putInventory(stack, (IInventory) entity, num, side);
        }
        return -1;
    }

    public static int suckItemInventoryWorld(IInventory target, int[] taccess, World world, BlockPos pos, @Nonnull EnumFacing side, int num) {
        return suckItemInventoryWorld(target, taccess, -1, world, pos, side, num);
    }

    public static int suckItemInventoryWorld(IInventory target, int[] taccess, int tfirst, World world, BlockPos pos, @Nonnull EnumFacing side, int num) {
        TileEntity entity = world.getTileEntity(pos);
        int moved = 0;
        if (entity instanceof IInventory) {
            for (int i = 0; i < taccess.length && moved < 1; i += 1) {
                ItemStack filter = target.getStackInSlot(taccess[i]);
                int num2 = Math.min(num, spaceForItem(filter, target, taccess));
                ItemStack mov = suckInventory(filter, (IInventory) entity, num2, side);
                if (!mov.isEmpty()) {
                    moved = mov.getCount();
                    int[] slots = sortAccessible(target, taccess, mov);
                    if (tfirst >= 0) slots = prioritizeAccessible(slots, tfirst);
                    putInventory(mov, target, 64, EnumFacing.UP, slots);
                }
            }
            return moved;
        }
        return -1;
    }

    @Nonnull
    public static ItemStack suckInventory(@Nonnull ItemStack filter, IInventory inv, int maxnum, @Nonnull EnumFacing side) {
        int[] slots = getAccessible(inv, side);
        ItemStack pulled = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory() && maxnum > 0; i += 1) {
            ItemStack slot = inv.getStackInSlot(slots[i]);
            if (!slot.isEmpty() && (filter.isEmpty() || filter.isItemEqual(slot))) {
                if (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(slots[i], slot, side)) {
                    int stackSize = slot.getCount();
                    if (filter.isEmpty()) filter = slot.copy();
                    if (maxnum >= stackSize) {
                        ItemStack stack = slot.copy();
                        inv.setInventorySlotContents(slots[i], ItemStack.EMPTY);
                        if (pulled.isEmpty()) pulled = stack;
                        else pulled = ItemUtil.sumItemStacks(stack, pulled, false);
                        maxnum -= stackSize;
                    } else {
                        if (pulled.isEmpty()) pulled = slot.splitStack(maxnum);
                        else pulled = ItemUtil.sumItemStacks(slot.splitStack(maxnum), pulled, false);
                        maxnum = 0;
                    }
                }
            }
        }
        return pulled;
    }

    public static int putInventory(ItemStack stack, IInventory inv, int maxnum, @Nonnull EnumFacing side) {
        int[] slots = getAccessible(inv, side);
        slots = sortAccessible(inv, slots, stack);
        return putInventory(stack, inv, maxnum, side, slots);
    }

    public static int putInventory(ItemStack stack, IInventory inv, int maxnum, @Nonnull EnumFacing side, int[] slots) {
        int maxcount = maxnum;
        for (int s : slots) {
            if (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(s, stack, side)) {
                if (!inv.isItemValidForSlot(s, stack)) continue;
                ItemStack slot = inv.getStackInSlot(s);
                if (slot.isEmpty() || slot.isItemEqual(stack)) {
                    int stackSize = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
                    int tstack = Math.min(stackSize, maxcount);
                    tstack = Math.min(tstack, stack.getCount());
                    if (tstack < 1) continue;
                    if (!slot.isEmpty()) {
                        tstack = Math.min(tstack, stackSize - slot.getCount());
                        slot.grow(tstack);
                        stack.shrink(tstack);
                    } else {
                        ItemStack copy = stack.copy();
                        stack.shrink(tstack);
                        copy.setCount(tstack);
                        inv.setInventorySlotContents(s, copy);
                    }
                    maxcount -= tstack;
                }
            }
        }
        return maxnum - maxcount;
    }

    public static int[] sortAccessible(IInventory inv, int[] slots, @Nonnull ItemStack stack) {
        int[] res = new int[slots.length];
        IntList sort = new IntArrayList();
        for (int s : slots) {    //Check all slots with matching Items
            ItemStack slot = inv.getStackInSlot(s);
            if (!slot.isEmpty() && slot.isItemEqual(stack))
                sort.add(s);
        }
        for (int s : slots) {    //Add all other slots
            if (!sort.contains(s))
                sort.add(s);
        }
        for (int i = 0; i < sort.size(); i += 1) {   //Convert the List to a Array
            if (i < res.length)
                res[i] = sort.getInt(i);
        }
        return res;
    }

    public static int[] prioritizeAccessible(int[] slots, int slot) {
        int[] res = new int[slots.length];
        IntList sort = new IntArrayList();
        for (int i = 0; i < slots.length; i += 1) {    //Check if slot is a accessible slot
            if (slots[i] == slot) {
                for (int j = i; j < slots.length; j += 1) {
                    sort.add(slots[j]);
                }
            }
        }
        for (int s : slots) {    //Add all other slots
            if (!sort.contains(s))
                sort.add(s);
        }
        for (int i = 0; i < sort.size(); i += 1) {   //Convert the List to a Array
            if (i < res.length)
                res[i] = sort.getInt(i);
        }
        return res;
    }

    public static int[] getAccessible(IInventory inv, EnumFacing side) {
        if (inv instanceof ISidedInventory && side != null)
            return ((ISidedInventory) inv).getSlotsForFace(side);
        int[] sides = new int[inv.getSizeInventory()];
        for (int i = 0; i < inv.getSizeInventory(); i += 1) {
            sides[i] = i;
        }
        return sides;
    }

    /*
     * Takes all items from invA and returns the matching slots in invB
     */
    public static int spaceForItem(ItemStack stack, IInventory inv, int[] slots) {
        int space = 0;
        int maxStack = Math.min(stack == null ? 64 : stack.getMaxStackSize(), inv.getInventoryStackLimit());
        for (int s : slots) {
            ItemStack slot = inv.getStackInSlot(s);
            if (slot.isEmpty())
                space += maxStack;
            else if (stack != null && slot.isItemEqual(stack)) {
                space += Math.max(0, maxStack - slot.getCount());
            }
        }
        return space;
    }

    public static void addToPlayerInventory(@Nonnull ItemStack stack, EntityPlayer player) {
        if (!stack.isEmpty()) {
            if (player.inventory.addItemStackToInventory(stack)) {
                player.inventory.markDirty();
                if (player.openContainer != null) {
                    player.openContainer.detectAndSendChanges();
                }
            }
            if (!stack.isEmpty()) {
                player.dropItem(stack, false);
            }
        }
    }


}
