package mods.computercarts.common.container;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.network.ManagedEnvironment;
import mods.computercarts.common.container.slots.ContainerSlot;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ComputerCartContainer extends Container {

    public static final int YSIZE_SCR = 256;
    public static final int YSIZE_NOSCR = 108;
    public static final int XSIZE = 256;
    public static final int DELTA = YSIZE_SCR - YSIZE_NOSCR;

    private EntityComputerCart entity;
    private EntityPlayer player;
    private boolean hasScreen = false;

    public int smaxEnergy = -1;
    public int sEnergy = -1;
    public int sizeinv = -1;
    public int selSlot = -1;
    public boolean updatesize = false;


    public ComputerCartContainer(InventoryPlayer inventory, EntityComputerCart entity) {
        this.entity = entity;
        this.player = inventory.player;

        this.initComponents(this.entity.getCompinv().getComponents());

        this.addSlotToContainer(new ContainerSlot(entity.compinv, 20, 188, 232 - ((this.hasScreen) ? 0 : DELTA), entity.compinv.getContainer(0)));
        this.addSlotToContainer(new ContainerSlot(entity.compinv, 21, 206, 232 - ((this.hasScreen) ? 0 : DELTA), entity.compinv.getContainer(1)));
        this.addSlotToContainer(new ContainerSlot(entity.compinv, 22, 224, 232 - ((this.hasScreen) ? 0 : DELTA), entity.compinv.getContainer(2)));

        for (int i = 0; i < entity.maininv.getMaxSizeInventory(); i += 1) {
            this.addSlotToContainer(new Slot(entity.maininv, i, -10000, -10000));
        }

        this.addPlayerInv(6, 174 - ((this.hasScreen) ? 0 : DELTA), inventory);
    }

    private void initComponents(Iterable<ManagedEnvironment> components) {
        for (ManagedEnvironment env : components) {
            if (env instanceof TextBuffer) this.hasScreen = true;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(entity) <= 64;
    }

    private void addPlayerInv(int x, int y, InventoryPlayer inventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventory, i, x + i * 18, y + 58));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        Slot s = this.getSlot(slot);
        if (s == null || !s.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = s.getStack();
        IntList a = getPlayerInvSlots(stack, player);
        IntList b = getCartInvSlots(stack);
        if (s.inventory.equals(player.inventory)) {
            for (int sl : b) {
                transferToSlot(sl, stack);
                if (stack.isEmpty()) {
                    s.putStack(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
            }
            for (int sl : a) {
                transferToSlot(sl, stack);
                if (stack.isEmpty()) {
                    s.putStack(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
            }
        } else {
            for (int sl : a) {
                transferToSlot(sl, stack);
                if (stack.isEmpty()) {
                    s.putStack(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
            }
            for (int sl : b) {
                transferToSlot(sl, stack);
                if (stack.isEmpty()) {
                    s.putStack(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) s.putStack(ItemStack.EMPTY);
        return stack;
    }

    private IntList getCartInvSlots(ItemStack stack) {
        IntList slots = new IntArrayList();
        for (Slot slot : this.inventorySlots) {
            if (slot.inventory.equals(this.entity.maininv) || slot.inventory.equals(this.entity.compinv)) {
                slots.add(slot.slotNumber);
            }
        }
        return sortSlots(stack, slots);
    }

    private IntList getPlayerInvSlots(ItemStack stack, EntityPlayer player) {
        IntList slots = new IntArrayList();
        for (Slot slot : this.inventorySlots) {
            if (slot.inventory.equals(player.inventory)) {
                slots.add(slot.slotNumber);
            }
        }
        return sortSlots(stack, slots);
    }

    private IntList sortSlots(ItemStack stack, IntList slots) {
        IntList res = new IntArrayList();
        for (int slot : slots) {
            Slot s = this.getSlot(slot);
            if (s.getHasStack() && s.getStack().isItemEqual(stack)) {
                res.add(slot);
            }
        }
        for (int slot : slots) {
            Slot s = this.getSlot(slot);
            if (!s.getHasStack() || !s.getStack().isItemEqual(stack)) {
                res.add(slot);
            }
        }
        return res;
    }

    private void transferToSlot(int slotindex, ItemStack stack) {
        Slot slot = this.getSlot(slotindex);
        if (slot == null || !slot.isItemValid(stack) || stack.isEmpty()) return;
        if (slot.getHasStack() && !slot.getStack().isItemEqual(stack)) return;
        int max = Math.min(slot.getSlotStackLimit(), Math.min(slot.inventory.getInventoryStackLimit(), stack.getMaxStackSize()));
        int cur = slot.getStack().getCount();
        int transfer = max - cur;
        transfer = Math.min(stack.getCount(), transfer);
        if (transfer > 0) {
            ItemStack copy = stack.copy();
            copy.setCount(transfer + cur);
            slot.putStack(copy);
            stack.shrink(transfer);
        }
    }

    public EntityComputerCart getEntity() {
        return this.entity;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public boolean getHasScreen() {
        return this.hasScreen;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);

        listener.sendWindowProperty(this, 0, (int) this.entity.getCurEnergy());
        listener.sendWindowProperty(this, 1, (int) this.entity.getMaxEnergy());
        listener.sendWindowProperty(this, 2, this.entity.getInventorySpace());
        listener.sendWindowProperty(this, 3, this.entity.selectedSlot());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (this.entity.world.isRemote) return;

        for (IContainerListener listener : this.listeners) {
            if (this.entity.getCurEnergy() != this.sEnergy) {
                listener.sendWindowProperty(this, 0, (int) (this.entity.getCurEnergy()));
            }

            if (this.entity.getMaxEnergy() != this.smaxEnergy) {
                listener.sendWindowProperty(this, 1, (int) (this.entity.getMaxEnergy()));
            }

            if (this.entity.getInventorySpace() != this.sizeinv) {
                listener.sendWindowProperty(this, 2, this.entity.getInventorySpace());
            }

            if (this.entity.selectedSlot() != this.selSlot) {
                listener.sendWindowProperty(this, 3, this.entity.selectedSlot());
            }
        }

        this.smaxEnergy = (int) this.entity.getMaxEnergy();
        this.sEnergy = (int) this.entity.getCurEnergy();
        this.sizeinv = this.entity.getInventorySpace();
        this.selSlot = this.entity.selectedSlot();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int field, int value) {
        switch (field) {
            case 0:
                this.sEnergy = value;
                break;
            case 1:
                this.smaxEnergy = value;
                break;
            case 2:
                this.updatesize = true;
                this.sizeinv = value;
                break;
            case 3:
                this.selSlot = value;
                break;
        }
    }

}
