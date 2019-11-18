package mods.computercarts.common.minecart;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import li.cil.oc.api.API;
import li.cil.oc.api.Manual;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.internal.Keyboard;
import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.network.*;
import li.cil.oc.common.Sound$;
import li.cil.oc.server.agent.Player;
import mods.computercarts.ComputerCarts;
import mods.computercarts.Settings;
import mods.computercarts.common.SyncEntity;
import mods.computercarts.common.blocks.NetRail;
import mods.computercarts.common.component.CartController;
import mods.computercarts.common.inventory.InventoryCartComponents;
import mods.computercarts.common.inventory.InventoryCartContainer;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.tank.TankCart;
import mods.computercarts.common.util.ComputerCartData;
import mods.computercarts.common.util.ItemUtil;
import mods.computercarts.common.util.RotationHelper;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.MessageEntitySyncRequest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityComputerCart extends EntityAdvancedCart implements MachineHost, Analyzable, SyncEntity, ComputerCart {
    protected static final DataParameter<Integer> LIGHT_COLOR = EntityDataManager.createKey(EntityComputerCart.class, DataSerializers.VARINT);
    protected static final DataParameter<Boolean> RUNNING = EntityDataManager.createKey(EntityComputerCart.class, DataSerializers.BOOLEAN);

    private final boolean isServer = FMLCommonHandler.instance().getEffectiveSide().isServer();

    private int tier = -1;    //The tier of the cart
    private Machine machine; //The machine object
    private boolean setup = true; //true if the update() function gets called the first time
    private boolean chDim = false;    //true if the cart changing the dimension (Portal, AE2 Storage,...)
    private CartController controller = new CartController(this); //The computer cart component
    private boolean onrail = false; // Store onRail from last tick to send a Signal
    private Player player; //OC's fake player

    private BlockPos cRail = BlockPos.ORIGIN;    // Position of the connected Network Rail
    private int cRailDim = 0;
    private boolean cRailCon = false; //True if the card is connected to a network rail
    private Node cRailNode = null; // This node will not get saved in NBT because it should automatic disconnect after restart;


    public InventoryCartComponents componentInventory = new InventoryCartComponents(this, 24) {
        @Override
        protected void onItemAdded(int slot, ItemStack stack) {
            super.onItemAdded(slot, stack);
            EntityComputerCart cart = EntityComputerCart.this;

            if (!cart.world.isRemote) {
                String slotType = getSlotType(slot);
                if (slotType.equals(Slot.Floppy)) {
                    Sound$.MODULE$.playDiskInsert(host);
                }
            }
        }

        @Override
        protected void onItemRemoved(int slot, ItemStack stack) {
            super.onItemRemoved(slot, stack);
            EntityComputerCart cart = EntityComputerCart.this;

            if (!cart.world.isRemote) {
                String slotType = getSlotType(slot);

                if (slotType.equals(Slot.Floppy)) {
                    Sound$.MODULE$.playDiskEject(host);
                }
            }
        }

        @Override
        public void markDirty() {
            super.markDirty();
            EntityComputerCart cart = EntityComputerCart.this;

            if (cart.world.isRemote) {
                ModNetwork.CHANNEL.sendToServer(new MessageEntitySyncRequest(cart));
            } else {
                mainInventory.recalculateSize();
                tanks.recalculateSize();
            }
        }

        @Override
        public void connectItemNode(Node node) {
            super.connectItemNode(node);
            if (node != null) {
                if (node.host() instanceof TextBuffer) {
                    for (int i = 0; i < getSizeInventory(); i += 1) {
                        if ((getSlotComponent(i) instanceof Keyboard) && getSlotComponent(i).node() != null)
                            node.connect(getSlotComponent(i).node());
                    }
                } else if (node.host() instanceof Keyboard) {
                    for (int i = 0; i < getSizeInventory(); i += 1) {
                        if ((getSlotComponent(i) instanceof TextBuffer) && getSlotComponent(i).node() != null)
                            node.connect(getSlotComponent(i).node());
                    }
                }
            }
        }
    };

    public InventoryBasic equipmentInventory = new InventoryBasic("equipment", false, 0);
    public InventoryCartContainer mainInventory = new InventoryCartContainer(this);
    public TankCart tanks = new TankCart(this);

    public EntityComputerCart(World world) {
        super(world);
    }

    public EntityComputerCart(World w, double x, double y, double z, @Nonnull ComputerCartData data) {
        super(w, x, y, z);
        this.tier = data.getTier();
        ((Connector) this.machine.node()).changeBuffer(data.getEnergy());
        //this.setEmblem(data.getEmblem());

        for (Int2ObjectMap.Entry<ItemStack> e : data.getComponents().int2ObjectEntrySet()) {
            if (e.getIntKey() < this.componentInventory.getSizeInventory() && !e.getValue().isEmpty()) {
                componentInventory.updateSlot(e.getIntKey(), e.getValue());
            }
        }

        this.mainInventory.recalculateSize();
        this.tanks.recalculateSize();
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        this.dataManager.register(LIGHT_COLOR, 0x0000FF);
        this.dataManager.register(RUNNING, false);

        this.machine = li.cil.oc.api.Machine.create(this);
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            this.machine.setCostPerTick(Settings.ComputerCartEnergyUse);
            ((Connector) this.machine.node()).setLocalBufferSize(Settings.ComputerCartEnergyCap);
        }

    }

    /*------NBT/Sync-Stuff-------*/
    @Override
    public void readEntityFromNBT(NBTTagCompound input) {
        super.readEntityFromNBT(input);

        if (input.hasKey("components")) this.componentInventory.readNBT((NBTTagList) input.getTag("components"));
        if (input.hasKey("controller")) this.controller.load(input.getCompoundTag("controller"));
        if (input.hasKey("inventory")) this.mainInventory.readFromNBT(input.getCompoundTag("inventory"));
        if (input.hasKey("netrail")) {
            NBTTagCompound netrail = input.getCompoundTag("netrail");
            this.cRailCon = true;
            this.cRail = new BlockPos(netrail.getInteger("posX"), netrail.getInteger("posY"), netrail.getInteger("posZ"));
            this.cRailDim = netrail.getInteger("posDim");
        }
        if (input.hasKey("settings")) {
            NBTTagCompound set = input.getCompoundTag("settings");
            if (set.hasKey("lightcolor")) this.setLightColor(set.getInteger("lightcolor"));
            if (set.hasKey("tier")) this.tier = set.getInteger("tier");
        }


        this.machine.onHostChanged();
        if (input.hasKey("machine")) this.machine.load(input.getCompoundTag("machine"));

        this.connectNetwork();
        this.mainInventory.recalculateSize();
        this.tanks.recalculateSize();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound output) {
        if (!this.isServer) return;

        super.writeEntityToNBT(output);

        this.componentInventory.saveComponents();

        output.setTag("components", this.componentInventory.writeNTB());
        output.setTag("inventory", this.mainInventory.writeToNBT());

        //Controller tag
        NBTTagCompound controller = new NBTTagCompound();
        this.controller.save(controller);
        output.setTag("controller", controller);

        //Data about the connected rail
        if (this.cRailCon) {
            NBTTagCompound netrail = new NBTTagCompound();
            netrail.setInteger("posX", this.cRail.getX());
            netrail.setInteger("posY", this.cRail.getY());
            netrail.setInteger("posZ", this.cRail.getZ());
            netrail.setInteger("posDim", this.cRailDim);
            output.setTag("netrail", netrail);
        } else if (output.hasKey("netrail")) output.removeTag("netrail");

        //Some additional values like light color, selected Slot, ...
        NBTTagCompound set = new NBTTagCompound();
        set.setInteger("lightcolor", this.getLightColor());
        set.setInteger("tier", this.tier);
        output.setTag("settings", set);

        NBTTagCompound machine = new NBTTagCompound();
        this.machine.save(machine);
        output.setTag("machine", machine);
    }

    @Override
    public NBTTagCompound writeSyncData(NBTTagCompound output) {
        this.componentInventory.saveComponents();
        output.setTag("components", this.componentInventory.writeNTB());
        return output;
    }

    @Override
    public void readSyncData(NBTTagCompound input) {
        this.componentInventory.readNBT((NBTTagList) input.getTag("components"));
        this.componentInventory.connectComponents();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) new InvWrapper(this.mainInventory);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.setup) {
            this.setup = false;

            if (this.world.isRemote) {
                ModNetwork.CHANNEL.sendToServer(new MessageEntitySyncRequest(this));
            } else {
                if (this.machine.node().network() == null) {
                    this.connectNetwork();
                }

                this.onrail = this.onRail();
                this.player = new li.cil.oc.server.agent.Player(this);
            }
        }

        if (!this.world.isRemote) {
            //Update the machine and the Components
            if (this.dataManager.get(RUNNING)) {
                this.machine.update();
                this.componentInventory.updateComponents();
            }
            //Check if the machine state has changed.
            if (this.dataManager.get(RUNNING) != this.machine.isRunning()) {
                this.dataManager.set(RUNNING, this.machine.isRunning());
                if (!this.dataManager.get(RUNNING)) this.setEngineSpeed(0);
            }
            //Consume energy for the Engine
            if (this.isEngineActive()) {
                if (!((Connector) this.machine.node()).tryChangeBuffer(-1.0 * this.getEngineSpeed() * Settings.ComputerCartEngineUse)) {
                    this.machine.signal("engine_failed", this.getEngineSpeed());
                    this.setEngineSpeed(0);
                }
            }
            //Check if the cart is on a Track
            if (this.onrail != this.onRail()) {
                this.onrail = !this.onrail;
                this.machine.signal("track_state", this.onrail);
            }
            //Give the cart energy if it is a creative cart
            if (this.tier == 3) ((Connector) this.machine.node()).changeBuffer(Integer.MAX_VALUE);
            //Connect / Disconnect a network rail
            this.checkRailConnection();
        }
    }

    private void connectNetwork() {
        API.network.joinNewNetwork(machine.node());
        this.componentInventory.connectComponents();
        this.machine.node().connect(this.controller.node());
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!this.world.isRemote && !this.chDim) {
            this.machine.stop();
            this.machine.node().remove();
            this.controller.node().remove();
            this.componentInventory.disconnectComponents();
            this.componentInventory.saveComponents();
            this.componentInventory.removeTagsForDrop();
        }
    }

    @Override
    public void killMinecart(DamageSource source) {
        super.killMinecart(source);
        List<ItemStack> drop = new ArrayList<>();
        for (int i = 20; i < 23; i += 1) {
            if (!componentInventory.getStackInSlot(i).isEmpty()) drop.add(componentInventory.getStackInSlot(i));
        }
        for (ItemStack item : this.mainInventory.removeOverflowItems(0)) drop.add(item);
        ItemUtil.dropItems(drop, this.world, this.posX, this.posY, this.posZ, true);
        this.setDamage(Float.MAX_VALUE); //Sometimes the cart stay alive this should fix it.
    }

    @Override
    public boolean processInitialInteract(EntityPlayer p, EnumHand hand) {
        ItemStack refMan = ModItems.getOCItem("manual");
        boolean openwiki = !p.getHeldItem(hand).isEmpty() && p.isSneaking() && p.getHeldItem(hand).getItem() == refMan.getItem() && p.getHeldItem(hand).getItemDamage() == refMan.getItemDamage();

        //if (Loader.isModLoaded("Railcraft") && RailcraftUtils.isUsingChrowbar(p)) return true;

        if (this.world.isRemote && openwiki) {
            Manual.navigate(ComputerCarts.MODID + "/%LANGUAGE%/item/cart.md");
            Manual.openFor(p);
        } else if (!this.world.isRemote && !openwiki) {
            p.openGui(ComputerCarts.INSTANCE, 1, this.world, this.getEntityId(), -10, 0);
        } else if (this.world.isRemote) {
            p.swingArm(hand);
        }
        return true;
    }

    @Override
    public Node[] onAnalyze(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        return new Node[]{this.machine.node()};
    }

    private void checkRailConnection() {
        //If the cart isn't connected check for a new connection
        BlockPos pos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
        if (!this.cRailCon && this.onRail() && (this.world.getBlockState(pos).getBlock() instanceof NetRail)) {
            NetRail netrail = (NetRail) this.world.getBlockState(pos).getBlock();
            if (netrail.isValid(this.world, pos, this) && netrail.getResponseEnvironment(this.world, pos) != null) {
                this.cRail = pos;
                this.cRailDim = this.world.provider.getDimension();
                this.cRailCon = true;
            }
        }
        //If the cart is connected to a rail check if the connection is still valid and connect or disconnect
        if (this.cRailCon) {
            World w = DimensionManager.getWorld(this.cRailDim);
            if (w.getBlockState(this.cRail).getBlock() instanceof NetRail) {
                NetRail netrail = (NetRail) w.getBlockState(this.cRail).getBlock();
                //Connect a new network Rail
                if (netrail.isValid(w, this.cRail, this) && netrail.getResponseEnvironment(w, this.cRail) != null) {
                    Node railnode = netrail.getResponseEnvironment(w, this.cRail).node();
                    if (!this.machine.node().canBeReachedFrom(railnode)) {
                        this.machine.node().connect(railnode);
                        this.cRailNode = railnode;
                        this.machine.signal("network_rail", true);
                    }
                }
                //Disconnect when the cart leaves a network rail
                else if (netrail.getResponseEnvironment(w, this.cRail) != null) {
                    Node railnode = netrail.getResponseEnvironment(w, this.cRail).node();
                    if (this.machine.node().canBeReachedFrom(railnode)) {
                        this.machine.node().disconnect(railnode);
                        this.cRailCon = false;
                        this.cRailNode = null;
                        this.machine.signal("network_rail", false);
                    }
                }
            }
            //Disconnect if the network rail is not there
            else {
                if (this.cRailNode != null && this.machine.node().canBeReachedFrom(this.cRailNode)) {
                    this.machine.node().disconnect(this.cRailNode);
                    this.cRailNode = null;
                    this.machine.signal("network_rail", false);
                }
                this.cRailCon = false;
            }
        }
    }

    @Override
    public ItemStack getCartItem() {
        ItemStack stack = new ItemStack(ModItems.COMPUTER_CART);

        Int2ObjectMap<ItemStack> components = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < 20; i += 1) {
            if (!componentInventory.getStackInSlot(i).isEmpty())
                components.put(i, componentInventory.getStackInSlot(i));
        }

        ComputerCartData data = new ComputerCartData();
        data.setEnergy(((Connector) this.machine().node()).localBuffer());
        data.setTier(this.tier);
        data.setComponents(components);
        ItemUtil.setCartData(stack, data);

        return stack;
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimension) {
        try {
            this.chDim = true;
            return super.changeDimension(dimension);
        } finally {
            this.chDim = false;
            this.setDead(); //FIXME ?
        }
    }

    @Override
    @Nonnull
    public ItemStack getPickedResult(RayTraceResult target) {
        return getCartItem();
    }

    /*----------------------------------*/

    /*--------MachineHost--------*/
    @Override
    public World world() {
        return this.world;
    }

    @Override
    public double xPosition() {
        return this.posX;
    }

    @Override
    public double yPosition() {
        return this.posY;
    }

    @Override
    public double zPosition() {
        return this.posZ;
    }

    @Override
    public void markChanged() {}

    @Override
    public Machine machine() {
        return this.machine;
    }

    @Override
    public Iterable<ItemStack> internalComponents() {
        List<ItemStack> components = new ArrayList<>();
        for (int i = 0; i < componentInventory.getSizeInventory(); i += 1) {
            if (!componentInventory.getStackInSlot(i).isEmpty() && this.componentInventory.isComponentSlot(i, componentInventory.getStackInSlot(i)))
                components.add(componentInventory.getStackInSlot(i));
        }
        return components;
    }

    @Override
    public int componentSlot(String address) {
        for (int i = 0; i < this.componentInventory.getSizeInventory(); i += 1) {
            ManagedEnvironment env = this.componentInventory.getSlotComponent(i);
            if (env != null && env.node() != null && env.node().address().equals(address)) return i;
        }
        return -1;
    }

    @Override
    public void onMachineConnect(Node node) {}

    @Override
    public void onMachineDisconnect(Node node) {}

    @Override
    public IInventory equipmentInventory() {
        return this.equipmentInventory;
    }

    @Override
    public IInventory mainInventory() {
        return this.mainInventory;
    }

    @Override
    public MultiTank tank() {
        return this.tanks;
    }

    @Override
    public int selectedSlot() {
        return this.mainInventory.getSelectedSlot();
    }

    @Override
    public void setSelectedSlot(int index) {
        if (index < this.mainInventory.getSizeInventory())
            this.setSelectedTank(index);
    }

    @Override
    public int selectedTank() {
        return this.tanks.getSelectedTank();
    }

    @Override
    public void setSelectedTank(int index) {
        if (index <= this.tank().tankCount())
            this.tanks.setSelectedTank(index);
    }

    @Override
    public EntityPlayer player() {
        Player.updatePositionAndRotation(player, this.facing(), this.facing());
        return this.player;
    }

    @Override
    public String name() {
        return this.getCustomNameTag();
    }

    @Override
    public void setName(String name) {
        this.setCustomNameTag(name);
    }

    @Override
    public String ownerName() {
        return li.cil.oc.Settings.get().fakePlayerName();
    }

    @Override
    public UUID ownerUUID() {
        return li.cil.oc.Settings.get().fakePlayerProfile().getId();
    }

    @Override
    public EnumFacing facing() {
        return RotationHelper.directionFromYaw(this.rotationYaw - 90D); //Minecarts seem to look at the right side
    }

    @Override
    public EnumFacing toGlobal(EnumFacing value) {
        return RotationHelper.calcGlobalDirection(value, this.facing());
    }

    @Override
    public EnumFacing toLocal(EnumFacing value) {
        return RotationHelper.calcLocalDirection(value, this.facing());
    }

    @Override
    public Node node() {
        return this.machine.node();
    }

    @Override
    public void onConnect(Node node) {}

    @Override
    public void onDisconnect(Node node) {}

    @Override
    public void onMessage(Message message) {}

    @Override
    public int tier() {
        return this.tier;
    }

    @Override
    public String getName() {
        return "inventory." + ComputerCarts.MODID + ".computercart";
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    /*-----Component-Inv------*/
    @Override
    public int componentCount() {
        int count = 0;
        for (ManagedEnvironment component : this.componentInventory.getComponents()) {
            count += 1;
        }
        return count;
    }

    @Override
    public Environment getComponentInSlot(int index) {
        if (index >= this.componentInventory.getSizeInventory()) return null;
        return this.componentInventory.getSlotComponent(index);
    }

    /*---------Railcraft---------*/

    public void lockdown(boolean lock) {
        super.lockdown(lock);
        if (lock != this.isLocked())
            this.machine.signal("cart_lockdown", lock);
    }

    /*------Setters & Getters-----*/
    public InventoryCartComponents getComponentInventory() {
        return this.componentInventory;
    }

    public void setRunning(boolean running) {
        if (this.world.isRemote) {
            this.dataManager.set(RUNNING, running);
        } else {
            if (running) {
                this.machine.start();
            } else {
                this.machine.stop();
            }
        }
    }

    public boolean isRunning() {
        return this.dataManager.get(RUNNING);
    }

    public double getCurEnergy() {
        if (!this.world.isRemote) return ((Connector) this.machine.node()).globalBuffer();
        return -1;
    }

    public double getMaxEnergy() {
        if (!this.world.isRemote) return ((Connector) this.machine.node()).globalBufferSize();
        return -1;
    }

    public int getInventorySpace() {
        return this.mainInventory.getSizeInventory();
    }

    public boolean getBrakeState() {
        return this.getBrake();
    }

    public void setBrakeState(boolean state) {
        this.setBrake(state);
    }

    public double getEngineState() {
        return this.getEngineSpeed();
    }

    public void setEngineState(double speed) {
        this.setEngineSpeed(speed);
    }

    public int getLightColor() {
        return this.dataManager.get(LIGHT_COLOR);
    }

    public void setLightColor(int color) {
        this.dataManager.set(LIGHT_COLOR, color);
    }

    public boolean hasNetRail() {
        return this.cRailCon;
    }

    @Override
    protected double addEnergy(double amount, boolean simulate) {
        Connector n = ((Connector) this.machine.node());
        double max = Math.min(n.globalBufferSize() - n.globalBuffer(), amount);
        if (!simulate) {
            max -= n.changeBuffer(max);
        }
        return max;
    }
}