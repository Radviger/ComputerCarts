package mods.computercarts.common.minecart;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import li.cil.oc.api.API;
import li.cil.oc.api.Manual;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Inventory;
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
import mods.computercarts.common.blocks.INetRail;
import mods.computercarts.common.component.ComputerCartController;
import mods.computercarts.common.driver.CustomDriver;
import mods.computercarts.common.inventory.ComponentInventory;
import mods.computercarts.common.inventory.ComputerCartInventory;
import mods.computercarts.common.items.ItemComputerCart;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.util.ComputerCartData;
import mods.computercarts.common.util.ItemUtil;
import mods.computercarts.common.util.RotationHelper;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.ComputercartInventoryUpdate;
import mods.computercarts.network.message.EntitySyncRequest;
import mods.computercarts.network.message.UpdateRunning;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityComputerCart extends EntityAdvancedCart implements MachineHost, Analyzable, SyncEntity, IComputerCart {
    protected static final DataParameter<Integer> LIGHT_COLOR = EntityDataManager.createKey(EntityComputerCart.class, DataSerializers.VARINT);

    private final boolean isServer = FMLCommonHandler.instance().getEffectiveSide().isServer();

    private int tier = -1;    //The tier of the cart
    private Machine machine; //The machine object
    private boolean firstupdate = true; //true if the update() function gets called the first time
    private boolean chDim = false;    //true if the cart changing the dimension (Portal, AE2 Storage,...)
    private boolean isRun = false; //true if the machine is turned on;
    private ComputerCartController controller = new ComputerCartController(this); //The computer cart component
    private double startEnergy = -1; //Only used when placing the cart. Start energy stored in the item
    private int invsize = 0; //The current inventory size depending on the Inventory Upgrades
    private boolean onrail = false; // Store onRail from last tick to send a Signal
    private int selSlot = 0; //The index of the current selected slot
    private int selTank = 1; //The index of the current selected tank
    private Player player; //OC's fake player
    private String name; //name of the cart

    private BlockPos cRail = BlockPos.ORIGIN;    // Position of the connected Network Rail
    private int cRailDim = 0;
    private boolean cRailCon = false; //True if the card is connected to a network rail
    private Node cRailNode = null; // This node will not get saved in NBT because it should automatic disconnect after restart;


    public ComponentInventory compinv = new ComponentInventory(this) {

        @Override
        public int getSizeInventory() {
            // 9 Upgrade Slots; 3 Container Slots; 8 Component Slots(CPU,Memory,...); 3 Provided Container Slots (the Container Slots in the GUI)
            return 23;
        }

        @Override
        protected void onItemAdded(int slot, ItemStack stack) {
            if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                super.onItemAdded(slot, stack);
                ((EntityComputerCart) this.host).synchronizeComponentSlot(slot);
            }

            if (this.getSlotType(slot).equals(Slot.Floppy)) Sound$.MODULE$.playDiskInsert(this.host);
            else if (this.getSlotType(slot).equals(Slot.Upgrade) && FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                DriverItem drv = CustomDriver.driverFor(stack, this.host.getClass());
                if (drv instanceof Inventory) {
                    ((EntityComputerCart) host).setInventorySpace(0);
                    ((EntityComputerCart) host).checkInventorySpace();
                }
            }
        }

        @Override
        protected void onItemRemoved(int slot, ItemStack stack) {
            super.onItemRemoved(slot, stack);
            if (FMLCommonHandler.instance().getEffectiveSide().isServer())
                ((EntityComputerCart) this.host).synchronizeComponentSlot(slot);

            if (this.getSlotType(slot).equals(Slot.Floppy)) Sound$.MODULE$.playDiskEject(this.host);
            else if (this.getSlotType(slot).equals(Slot.Upgrade) && FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                DriverItem drv = CustomDriver.driverFor(stack, this.host.getClass());
                if (drv instanceof Inventory) {
                    ((EntityComputerCart) host).setInventorySpace(0);
                    ((EntityComputerCart) host).checkInventorySpace();
                }
            }
        }

        @Override
        public void connectItemNode(Node node) {
            super.connectItemNode(node);
            if (node != null) {
                if (node.host() instanceof TextBuffer) {
                    for (int i = 0; i < this.getSizeInventory(); i += 1) {
                        if ((this.getSlotComponent(i) instanceof Keyboard) && this.getSlotComponent(i).node() != null)
                            node.connect(this.getSlotComponent(i).node());
                    }
                } else if (node.host() instanceof Keyboard) {
                    for (int i = 0; i < this.getSizeInventory(); i += 1) {
                        if ((this.getSlotComponent(i) instanceof TextBuffer) && this.getSlotComponent(i).node() != null)
                            node.connect(this.getSlotComponent(i).node());
                    }
                }
            }
        }
    };

    public ComputerCartInventory maininv = new ComputerCartInventory(this);

    public MultiTank tanks = new MultiTank() {
        @Override
        public int tankCount() {
            return EntityComputerCart.this.tankcount();
        }

        @Override
        public IFluidTank getFluidTank(int index) {
            return EntityComputerCart.this.getTank(index);
        }
    };

    public EntityComputerCart(World world) {
        super(world);
    }

    public EntityComputerCart(World w, double x, double y, double z, ComputerCartData data) {
        super(w, x, y, z);
        if (data == null) {
            this.setDead();
            data = new ComputerCartData();
        }
        this.tier = data.getTier();
        this.startEnergy = data.getEnergy();
        //this.setEmblem(data.getEmblem());

        for (Int2ObjectMap.Entry<ItemStack> e : data.getComponents().int2ObjectEntrySet()) {
            if (e.getIntKey() < this.compinv.getSizeInventory() && e.getValue() != null) {
                compinv.updateSlot(e.getIntKey(), e.getValue());
            }
        }

        this.checkInventorySpace();
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        this.dataManager.register(LIGHT_COLOR, 0x0000FF);

        this.machine = li.cil.oc.api.Machine.create(this);
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            this.machine.setCostPerTick(Settings.ComputerCartEnergyUse);
            ((Connector) this.machine.node()).setLocalBufferSize(Settings.ComputerCartEnergyCap);
        }

    }

    /*------NBT/Sync-Stuff-------*/
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("components")) this.compinv.readNBT((NBTTagList) nbt.getTag("components"));
        if (nbt.hasKey("controller")) this.controller.load(nbt.getCompoundTag("controller"));
        if (nbt.hasKey("inventory")) this.maininv.readFromNBT((NBTTagList) nbt.getTag("inventory"));
        if (nbt.hasKey("netrail")) {
            NBTTagCompound netrail = nbt.getCompoundTag("netrail");
            this.cRailCon = true;
            this.cRail = new BlockPos(netrail.getInteger("posX"), netrail.getInteger("posY"), netrail.getInteger("posZ"));
            this.cRailDim = netrail.getInteger("posDim");
        }
        if (nbt.hasKey("settings")) {
            NBTTagCompound set = nbt.getCompoundTag("settings");
            if (set.hasKey("lightcolor")) this.setLightColor(set.getInteger("lightcolor"));
            if (set.hasKey("selectedslot")) this.selSlot = set.getInteger("selectedslot");
            if (set.hasKey("selectedtank")) this.selTank = set.getInteger("selectedtank");
            if (set.hasKey("tier")) this.tier = set.getInteger("tier");
        }


        this.machine.onHostChanged();
        if (nbt.hasKey("machine")) this.machine.load(nbt.getCompoundTag("machine"));

        this.connectNetwork();
        this.checkInventorySpace();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        if (!this.isServer) return;

        super.writeEntityToNBT(nbt);

        this.compinv.saveComponents();

        nbt.setTag("components", this.compinv.writeNTB());
        nbt.setTag("inventory", this.maininv.writeToNBT());

        //Controller tag
        NBTTagCompound controller = new NBTTagCompound();
        this.controller.save(controller);
        nbt.setTag("controller", controller);

        //Data about the connected rail
        if (this.cRailCon) {
            NBTTagCompound netrail = new NBTTagCompound();
            netrail.setInteger("posX", this.cRail.getX());
            netrail.setInteger("posY", this.cRail.getY());
            netrail.setInteger("posZ", this.cRail.getZ());
            netrail.setInteger("posDim", this.cRailDim);
            nbt.setTag("netrail", netrail);
        } else if (nbt.hasKey("netrail")) nbt.removeTag("netrail");

        //Some additional values like light color, selected Slot, ...
        NBTTagCompound set = new NBTTagCompound();
        set.setInteger("lightcolor", this.getLightColor());
        set.setInteger("selectedslot", this.selSlot);
        set.setInteger("selectedtank", this.selTank);
        set.setInteger("tier", this.tier);
        nbt.setTag("settings", set);

        NBTTagCompound machine = new NBTTagCompound();
        this.machine.save(machine);
        nbt.setTag("machine", machine);
    }

    @Override
    public void writeSyncData(NBTTagCompound nbt) {
        this.compinv.saveComponents();
        nbt.setTag("components", this.compinv.writeNTB());
        nbt.setBoolean("isRunning", this.isRun);
    }

    @Override
    public void readSyncData(NBTTagCompound nbt) {
        this.compinv.readNBT((NBTTagList) nbt.getTag("components"));
        this.isRun = nbt.getBoolean("isRunning");
        this.compinv.connectComponents();
    }

    /*--------------------*/

    /*------Interaction-------*/

    protected void checkInventorySpace() {
        for (int i = 0; i < this.compinv.getSizeInventory(); i += 1) {
            if (!this.compinv.getStackInSlot(i).isEmpty()) {
                ItemStack stack = this.compinv.getStackInSlot(i);
                DriverItem drv = CustomDriver.driverFor(stack, this.getClass());
                if (drv instanceof Inventory && this.invsize < this.maininv.getMaxSizeInventory()) {
                    this.invsize = this.invsize + ((Inventory) drv).inventoryCapacity(stack);
                    if (this.invsize > this.maininv.getMaxSizeInventory())
                        this.invsize = this.maininv.getMaxSizeInventory();
                }
            }
        }
        Iterable<ItemStack> over = this.maininv.removeOverflowItems(this.invsize);
        ItemUtil.dropItems(over, this.world, this.posX, this.posY, this.posZ, true);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //Only executed at the first function call
        if (this.firstupdate) {
            this.firstupdate = false;
            //Request a entity data sync
            if (this.world.isRemote) ModNetwork.CHANNEL.sendToServer(new EntitySyncRequest(this));
            else {
                if (this.startEnergy > 0)
                    ((Connector) this.machine.node()).changeBuffer(this.startEnergy); //Give start energy
                if (this.machine.node().network() == null) {
                    this.connectNetwork(); //Connect all nodes (Components & Controller)
                }

                this.onrail = this.onRail();  //Update onRail Value
                this.player = new li.cil.oc.server.agent.Player(this);  //Set the fake Player
            }
        }

        if (!this.world.isRemote) {
            //Update the machine and the Components
            if (this.isRun) {
                this.machine.update();
                this.compinv.updateComponents();
            }
            //Check if the machine state has changed.
            if (this.isRun != this.machine.isRunning()) {
                this.isRun = this.machine.isRunning();
                ModNetwork.sendToNearPlayers(new UpdateRunning(this, this.isRun), this.posX, this.posY, this.posZ, this.world);
                if (!this.isRun) this.setEngineSpeed(0);
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
        this.compinv.connectComponents();
        this.machine.node().connect(this.controller.node());
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!this.world.isRemote && !this.chDim) {
            this.machine.stop();
            this.machine.node().remove();
            this.controller.node().remove();
            this.compinv.disconnectComponents();
            this.compinv.saveComponents();
            this.compinv.removeTagsForDrop();
        }
    }

    @Override
    public void killMinecart(DamageSource source) {
        super.killMinecart(source);
        List<ItemStack> drop = new ArrayList<>();
        for (int i = 20; i < 23; i += 1) {
            if (!compinv.getStackInSlot(i).isEmpty()) drop.add(compinv.getStackInSlot(i));
        }
        for (ItemStack item : this.maininv.removeOverflowItems(0)) drop.add(item);
        ItemUtil.dropItems(drop, this.world, this.posX, this.posY, this.posZ, true);
        this.setDamage(Float.MAX_VALUE); //Sometimes the cart stay alive this should fix it.
    }

    @Override
    public boolean processInitialInteract(EntityPlayer p, EnumHand hand) {
        ItemStack refMan = API.items.get("manual").createItemStack(1);
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
        if (!this.cRailCon && this.onRail() && (this.world.getBlockState(pos).getBlock() instanceof INetRail)) {
            INetRail netrail = (INetRail) this.world.getBlockState(pos).getBlock();
            if (netrail.isValid(this.world, pos, this) && netrail.getResponseEnvironment(this.world, pos) != null) {
                this.cRail = pos;
                this.cRailDim = this.world.provider.getDimension();
                this.cRailCon = true;
            }
        }
        //If the cart is connected to a rail check if the connection is still valid and connect or disconnect
        if (this.cRailCon) {
            World w = DimensionManager.getWorld(this.cRailDim);
            if (w.getBlockState(this.cRail).getBlock() instanceof INetRail) {
                INetRail netrail = (INetRail) w.getBlockState(this.cRail).getBlock();
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

    /*------------------------*/

    /*-----Minecart/Entity-Stuff-------*/

    @Override
    public Type getType() {
        return null;
    }

    public static EntityMinecart create(World w, double x, double y, double z, ComputerCartData data) {
        return new EntityComputerCart(w, x, y, z, data);
    }

    @Override
    public ItemStack getCartItem() {
        ItemStack stack = new ItemStack(ModItems.COMPUTER_CART);


        Int2ObjectMap<ItemStack> components = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < 20; i += 1) {
            if (!compinv.getStackInSlot(i).isEmpty())
                components.put(i, compinv.getStackInSlot(i));
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
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return null;
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
    public void markChanged() {
    }

    @Override
    public Machine machine() {
        return this.machine;
    }

    @Override
    public Iterable<ItemStack> internalComponents() {
        ArrayList<ItemStack> components = new ArrayList<>();
        for (int i = 0; i < compinv.getSizeInventory(); i += 1) {
            if (compinv.getStackInSlot(i) != null && this.compinv.isComponentSlot(i, compinv.getStackInSlot(i)))
                components.add(compinv.getStackInSlot(i));
        }
        return components;
    }

    @Override
    public int componentSlot(String address) {
        for (int i = 0; i < this.compinv.getSizeInventory(); i += 1) {
            ManagedEnvironment env = this.compinv.getSlotComponent(i);
            if (env != null && env.node() != null && env.node().address().equals(address)) return i;
        }
        return -1;
    }

    @Override
    public void onMachineConnect(Node node) {
    }

    @Override
    public void onMachineDisconnect(Node node) {
    }

    @Override
    public IInventory equipmentInventory() {
        return null;
    }

    @Override
    public IInventory mainInventory() {
        return this.maininv;
    }

    @Override
    public MultiTank tank() {
        return this.tanks;
    }

    @Override
    public int selectedSlot() {
        return this.selSlot;
    }

    @Override
    public void setSelectedSlot(int index) {
        if (index < this.maininv.getSizeInventory())
            this.selSlot = index;
    }

    @Override
    public int selectedTank() {
        return this.selTank;
    }

    @Override
    public void setSelectedTank(int index) {
        if (index <= this.tank().tankCount())
            this.selTank = index;
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
    /*-----------------------------*/

    /*-------Inventory--------*/

    @Override
    public int getSizeInventory() {
        return this.maininv.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return this.maininv.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.maininv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return this.maininv.decrStackSize(slot, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return this.maininv.removeStackFromSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.maininv.setInventorySlotContents(slot, stack);
    }

    @Override
    public String getName() {
        return "inventory." + ComputerCarts.MODID + ".computercart";
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public int getInventoryStackLimit() {
        return this.maininv.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return player.getDistanceSq(this) <= 64 && !this.isDead;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {}

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return this.maininv.isItemValidForSlot(slot, stack);
    }

    @Override
    public int getField(int field) {
        return this.maininv.getField(field);
    }

    @Override
    public void setField(int field, int value) {
        this.maininv.setField(field, value);
    }

    @Override
    public int getFieldCount() {
        return this.maininv.getFieldCount();
    }

    @Override
    public void clear() {
        this.maininv.clear();
    }

    /*------Tanks-------*/

    public int tankcount() {
        int c = 0;
        for (int i = 0; i < this.compinv.getSizeInventory(); i += 1) {
            if (this.compinv.getSlotComponent(i) instanceof IFluidTank) {
                c += 1;
            }
        }
        return c;
    }

    public IFluidTank getTank(int index) {
        int c = 0;
        for (int i = 0; i < this.compinv.getSizeInventory(); i += 1) {
            if (this.compinv.getSlotComponent(i) instanceof IFluidTank) {
                c += 1;
                if (c == index) return (IFluidTank) this.compinv.getSlotComponent(i);
            }
        }
        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack fluidStack, boolean b) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int i, boolean b) {
        return null;
    }
    /*--------------------*/

    /*-----Component-Inv------*/
    @Override
    public int componentCount() {
        int count = 0;
        for (ManagedEnvironment managedEnvironment : this.compinv.getComponents()) {
            count += 1;
        }
        return count;
    }

    @Override
    public Environment getComponentInSlot(int index) {
        if (index >= this.compinv.getSizeInventory()) return null;
        return this.compinv.getSlotComponent(index);
    }

    @Override
    public void synchronizeComponentSlot(int slot) {
        if (!this.world.isRemote)
            ModNetwork.sendToNearPlayers(new ComputercartInventoryUpdate(this, slot, this.compinv.getStackInSlot(slot)), this.posX, this.posY, this.posZ, this.world);
    }

    /*---------Railcraft---------*/

    public void lockdown(boolean lock) {
        super.lockdown(lock);
        if (lock != this.isLocked())
            this.machine.signal("cart_lockdown", lock);
    }

    /*------Setters & Getters-----*/
    public ComponentInventory getCompinv() {
        return this.compinv;
    }

    public void setRunning(boolean running) {
        if (this.world.isRemote) this.isRun = running;
        else {
            if (running) this.machine.start();
            else this.machine.stop();
        }
    }

    public boolean getRunning() {
        return this.isRun;
    }

    public double getCurEnergy() {
        if (!this.world.isRemote) return ((Connector) this.machine.node()).globalBuffer();
        return -1;
    }

    public double getMaxEnergy() {
        if (!this.world.isRemote) return ((Connector) this.machine.node()).globalBufferSize();
        return -1;
    }

    protected void setInventorySpace(int invsize) {
        this.invsize = invsize;
    }

    public int getInventorySpace() {
        return this.invsize;
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