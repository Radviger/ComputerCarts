package mods.computercarts.common.container.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotGhost extends Slot {

    public SlotGhost(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getSlotStackLimit() {
        return 0;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        ItemStack result = super.onTake(player, stack);
        player.inventory.setItemStack(ItemStack.EMPTY);
        return result;
    }

    @Override
    public void putStack(ItemStack stack) {
        if (!stack.isEmpty()) stack.setCount(1);
        super.putStack(stack);
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = inventory.getStackInSlot(this.getSlotIndex());
        if (!stack.isEmpty()) stack.setCount(0);
        return stack;
    }
}
