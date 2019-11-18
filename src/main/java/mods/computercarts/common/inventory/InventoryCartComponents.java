package mods.computercarts.common.inventory;

import li.cil.oc.api.API;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.Container;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.common.Tier;
import li.cil.oc.common.component.Screen;
import mods.computercarts.ComputerCarts;
import mods.computercarts.Settings;
import mods.computercarts.common.driver.CustomDriver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class InventoryCartComponents implements IInventory, Environment {

    protected MachineHost host;
    private final NonNullList<ItemStack> slots;
    private final ManagedEnvironment[] components;

    private List<ManagedEnvironment> updatingComponents = new ArrayList<>();

    public InventoryCartComponents(MachineHost host, int size) {
        this.host = host;
        this.slots = NonNullList.withSize(size, ItemStack.EMPTY);
        this.components = new ManagedEnvironment[size];
    }

    @Override
    public int getSizeInventory() {
        return slots.size();
    }

    @Override
    public @Nonnull ItemStack getStackInSlot(int slot) {
        if (slot < getSizeInventory()) return slots.get(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack decrStackSize(int slot, int amount) {
        ItemStack result = ItemStackHelper.getAndSplit(slots, slot, amount);
        if (!result.isEmpty() && slots.get(slot).isEmpty()) {
            onItemRemoved(slot, result);
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        if (slot >= 0 && slot < getSizeInventory()) {
            if (stack.isEmpty() && slots.get(slot).isEmpty()) return;
            if (!slots.get(slot).isEmpty() && !stack.isEmpty() && slots.get(slot) == stack) return;

            ItemStack oldStack = slots.get(slot);
            this.updateSlot(slot, ItemStack.EMPTY);
            if (!oldStack.isEmpty()) this.onItemRemoved(slot, oldStack);

            if (!stack.isEmpty()) {
                if (stack.getCount() >= this.getInventoryStackLimit()) {
                    stack.setCount(this.getInventoryStackLimit());
                }
                this.updateSlot(slot, stack);
            }

            if (!slots.get(slot).isEmpty()) {
                this.onItemAdded(slot, stack);
            }

            this.markDirty();
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return ItemStackHelper.getAndRemove(slots, slot);
    }

    @Override
    public String getName() {
        return "Component Inventory";
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public boolean isEmpty() {
        return slots.isEmpty();
    }

    @Override
    public void clear() {
        for (int i = 0; i < slots.size(); i++) {
            ItemStack slot = slots.remove(i);
            onItemRemoved(i, slot);
        }
    }

    @Override
    public int getField(int i) {
        return 0;
    }

    @Override
    public void setField(int i, int i1) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    public void updateSlot(int slot, @Nonnull ItemStack stack) {
        slots.set(slot, stack);
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        DriverItem driver = CustomDriver.driverFor(stack, host.getClass());
        return driver != null && (driver.slot(stack).equals(this.getSlotType(slot)) || this.getSlotType(slot).equals(Slot.Any)) && driver.tier(stack) <= this.getSlotTier(slot);
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public String getSlotType(int slot) {
        if (slot >= 20 && slot <= 22) {
            DriverItem drv = CustomDriver.driverFor(this.getContainer(slot - 20));
            if (drv instanceof Container) {
                return ((Container) drv).providedSlot(this.getContainer(slot - 20));
            } else return Slot.None;
        }
        return Slot.Any;
    }

    public int getSlotTier(int slot) {
        if (slot >= 20 && slot <= 22) {
            DriverItem drv = CustomDriver.driverFor(this.getContainer(slot - 20));
            if (drv instanceof Container) {
                return ((Container) drv).providedTier(this.getContainer(slot - 20));
            } else return Tier.None();
        }
        return Tier.Any();
    }

    public Node node() {
        return this.host.machine().node();
    }

    public void updateComponents() {
        if (!this.updatingComponents.isEmpty()) {
            for (ManagedEnvironment component : this.updatingComponents) {
                component.update();
            }
        }
    }

    public ManagedEnvironment getSlotComponent(int slot) {
        if (slot < getSizeInventory()) return this.components[slot];
        return null;
    }

    public void connectComponents() {
        for (int slot = 0; slot < getSizeInventory(); slot += 1) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty() && this.components[slot] == null && this.isComponentSlot(slot, stack)) {
                DriverItem drv = CustomDriver.driverFor(stack, host.getClass());
                if (drv != null) {
                    ManagedEnvironment env = drv.createEnvironment(stack, host);
                    if (env != null) {
                        try {
                            env.load(dataTag(drv, stack));
                        } catch (Throwable e) {
                            ComputerCarts.LOGGER.warn("An item component of type" + env.getClass().getName() + " (provided by driver " + drv.getClass().getName() + ") threw an error while loading.", e);
                        }
                        this.components[slot] = env;
                        if (env.canUpdate() && !this.updatingComponents.contains(env)) {
                            this.updatingComponents.add(env);
                        }
                    }
                }
            }
        }

        API.network.joinNewNetwork(this.node());
        for (ManagedEnvironment component : this.components) {
            if (component != null && component.node() != null) {
                this.connectItemNode(component.node());
            }
        }
    }

    public void disconnectComponents() {
        for (ManagedEnvironment component : this.components) {
            if (component != null) component.node().remove();
        }
    }

    public void connectItemNode(Node node) {
        if (this.node() != null && node != null && this.node().network() != null) {
            this.node().connect(node);
        }
    }

    public void removeTagsForDrop() {
        for (int i = 0; i < this.getSizeInventory(); i += 1) {
            if (!this.getStackInSlot(i).isEmpty()) {
                DriverItem drv = CustomDriver.driverFor(this.getStackInSlot(i), this.host.getClass());
                //Unfortunately it's not possible to make 'instanceof' with a Scala class and I'am lazy. So I check the Environment class.
                if ((drv instanceof EnvironmentProvider) && ((EnvironmentProvider) drv).getEnvironment(this.getStackInSlot(i)) == Screen.class) {
                    NBTTagCompound tag = this.dataTag(drv, this.getStackInSlot(i));

                    Set<String> tags = tag.getKeySet();
                    String[] keys = tags.toArray(new String[0]);
                    for (String s : keys) {
                        tag.removeTag(s);
                    }
                }
            }
        }
    }

    synchronized protected void onItemAdded(int slot, ItemStack stack) {
        DriverItem drv = CustomDriver.driverFor(stack, host.getClass());
        if (drv != null) {
            ManagedEnvironment env = drv.createEnvironment(stack, host);
            if (env != null) {
                try {
                    env.load(this.dataTag(drv, stack));
                } catch (Throwable e) {
                    ComputerCarts.LOGGER.warn("An item component of type" + env.getClass().getName() + " (provided by driver " + drv.getClass().getName() + ") threw an error while loading.", e);
                }

                this.components[slot] = env;
                this.connectItemNode(env.node());
                if (env.canUpdate() && !this.updatingComponents.contains(env)) {
                    this.updatingComponents.add(env);
                }
                this.save(env, drv, stack);
            }
        }
    }

    synchronized protected void onItemRemoved(int slot, ItemStack stack) {
        if (this.components[slot] != null && FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            ManagedEnvironment component = this.components[slot];
            this.components[slot] = null;
            if (this.updatingComponents != null)
                this.updatingComponents.remove(component);
            component.node().remove();
            this.save(component, CustomDriver.driverFor(stack), stack);
            component.node().remove();
        }
    }

    private NBTTagCompound dataTag(DriverItem driver, ItemStack stack) {
        NBTTagCompound tag = null;
        if (driver != null) tag = driver.dataTag(stack);
        if (tag == null) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound nbt = stack.getTagCompound();
            if (!nbt.hasKey(Settings.OC_Namespace + "data")) {
                nbt.setTag(Settings.OC_Namespace + "data", new NBTTagCompound());
            }
            tag = nbt.getCompoundTag(Settings.OC_Namespace + "data");
        }
        return tag;
    }

    public boolean isComponentSlot(int slot, ItemStack stack) {
        return true;
    }

    public void save(ManagedEnvironment component, DriverItem driver, ItemStack stack) {
        try {
            NBTTagCompound tag = this.dataTag(driver, stack);

            Set<String> tags = tag.getKeySet();
            String[] keys = tags.toArray(new String[0]);
            for (String s : keys) {
                tag.removeTag(s);
            }
            component.save(tag);
        } catch (Throwable e) {
            ComputerCarts.LOGGER.warn("An item component of type " + component.getClass().getName() + " (provided by driver " + driver.getClass().getName() + ") threw an error while loading.", e);
        }
    }

    public NBTTagList writeNTB() {
        NBTTagList nbt = new NBTTagList();
        for (byte slot = 0; slot < this.getSizeInventory(); slot += 1) {
            NBTTagCompound invslot = new NBTTagCompound();
            NBTTagCompound item = new NBTTagCompound();
            invslot.setByte("slot", slot);
            if (!this.getStackInSlot(slot).isEmpty()) {
                this.getStackInSlot(slot).writeToNBT(item);
                invslot.setTag("item", item);
            }
            nbt.appendTag(invslot);
        }
        return nbt;
    }

    public void readNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i += 1) {
            NBTTagCompound invslot = nbt.getCompoundTagAt(i);
            ItemStack stack = new ItemStack(invslot.getCompoundTag("item"));
            byte slot = invslot.getByte("slot");
            if (slot >= 0 && slot < this.getSizeInventory()) this.updateSlot(slot, stack);
        }
    }

    public Iterable<ManagedEnvironment> getComponents() {
        List<ManagedEnvironment> list = new ArrayList<>();
        for (int i = 0; i < this.getSizeInventory(); i += 1) {
            if (this.components[i] != null) {
                list.add(this.components[i]);
            }
        }
        return list;
    }

    public void saveComponents() {
        for (int slot = 0; slot < this.getSizeInventory(); slot += 1) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty() && this.components[slot] != null) {
                this.save(this.components[slot], CustomDriver.driverFor(stack, host.getClass()), stack);
            }
        }
    }

    public @Nonnull ItemStack getContainer(int index) {
        if (index >= 0 && index <= 2 && !this.getStackInSlot(index).isEmpty()) {
            DriverItem it = CustomDriver.driverFor(this.getStackInSlot(index));
            if (it instanceof Container) return this.getStackInSlot(index);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onConnect(Node node) {}

    @Override
    public void onDisconnect(Node node) {}

    @Override
    public void onMessage(Message message) {}
}
