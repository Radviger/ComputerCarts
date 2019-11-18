package mods.computercarts.common.component;

import li.cil.oc.api.API;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.*;
import mods.computercarts.Settings;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.util.InventoryUtil;
import mods.computercarts.common.util.ItemUtil;
import mods.computercarts.common.util.TankUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;


public class CartController implements ManagedEnvironment {

    private Node node;
    private EntityComputerCart cart;

    public CartController(EntityComputerCart cart) {
        this.cart = cart;
        this.node = API.network.newNode(this, Visibility.Neighbors).withComponent("computercart").create();
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public void onConnect(Node node) {
    }

    @Override
    public void onDisconnect(Node node) {
    }

    @Override
    public void onMessage(Message message) {
    }

    @Override
    public void load(NBTTagCompound input) {
        if (input.hasKey("node")) this.node.load(input.getCompoundTag("node"));
    }

    @Override
    public void save(NBTTagCompound output) {
        NBTTagCompound node = new NBTTagCompound();
        this.node.save(node);
        output.setTag("node", node);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void update() {
    }

    /*--------Component-Functions-Cart--------*/

    @Callback(doc = "function(set:boolean):boolen, string -- Enable/Disable the brake. String for errors")
    public Object[] setBrake(Context context, Arguments arguments) {
        boolean state = arguments.checkBoolean(0);
        if (this.cart.getSpeed() > this.cart.getMaxCartSpeedOnRail() && state) {
            return new Object[]{this.cart.getBrakeState(), "too fast"};
        }
        this.cart.setBrakeState(state);
        return new Object[]{state, null};
    }

    @Callback(direct = true, doc = "function():boolean -- Get the status of the brake.")
    public Object[] getBrake(Context context, Arguments arguments) {
        return new Object[]{this.cart.getBrakeState()};
    }

    @Callback(direct = true, doc = "function():number -- Get engine speed")
    public Object[] getEngineSpeed(Context context, Arguments arguments) {
        return new Object[]{this.cart.getEngineState()};
    }

    @Callback(doc = "function():number -- Get current speed of the cart. -1 if there is no rail")
    public Object[] getCartSpeed(Context context, Arguments arguments) {
        double speed = -1;
        if (this.cart.onRail()) {
            speed = this.cart.getSpeed();
        }
        return new Object[]{speed};
    }

    @Callback(doc = "function(speed:number):number -- Set the engine speed.")
    public Object[] setEngineSpeed(Context context, Arguments arguments) {
        double speed = Math.max(Math.min(arguments.checkDouble(0), this.cart.getMaxCartSpeedOnRail()), 0);
        this.cart.setEngineState(speed);
        return new Object[]{speed};
    }

    @Callback(doc = "function():number -- Get the maximal cart speed")
    public Object[] getMaxSpeed(Context context, Arguments arguments) {
        return new Object[]{this.cart.getMaxCartSpeedOnRail()};
    }

    @Callback(doc = "function(color:number):number -- Set light color")
    public Object[] setLightColor(Context context, Arguments arguments) {
        int color = arguments.checkInteger(0);
        this.cart.setLightColor(color);
        return new Object[]{color};
    }

    @Callback(direct = true, doc = "function():number -- Get light color")
    public Object[] getLightColor(Context context, Arguments arguments) {
        return new Object[]{this.cart.getLightColor()};
    }

    @Callback(doc = "function() -- Rotate the cart")
    public Object[] rotate(Context context, Arguments arguments) {
        float yaw = this.cart.rotationYaw + 180F;
        if (yaw > 180) yaw -= 360F;
        else if (yaw < -180) yaw += 360F;
        this.cart.rotationYaw = yaw;
        //ComputerCarts.LOGGER.info("Rotate: "+this.cart.rotationYaw+" + "+cart.facing().toString());
        return new Object[]{};
    }

    @Callback(direct = true, doc = "function():boolean -- Check if the cart is on a rail")
    public Object[] onRail(Context context, Arguments arguments) {
        return new Object[]{this.cart.onRail()};
    }

    @Callback(direct = true, doc = "function():boolean -- Check if the cart is connected to a network rail")
    public Object[] hasNetworkRail(Context context, Arguments arguments) {
        return new Object[]{this.cart.hasNetRail()};
    }

    @Callback(direct = true, doc = "function():boolean -- Check if the cart is locked on a track")
    public Object[] isLocked(Context context, Arguments arguments) {
        return new Object[]{this.cart.isLocked()};
    }

    /*--------Component-Functions-Inventory--------*/

    @Callback(doc = "function():number -- The size of this device's internal inventory.")
    public Object[] inventorySize(Context context, Arguments arguments) {
        return new Object[]{this.cart.getInventorySpace()};
    }

    @Callback(doc = "function([slot:number]):number -- Get the currently selected slot; set the selected slot if specified.")
    public Object[] select(Context context, Arguments args) {
        int slot = args.optInteger(0, 0);
        if (slot > 0 && slot <= this.cart.mainInventory.getMaxSizeInventory()) {
            this.cart.setSelectedSlot(slot - 1);
        } else if (args.count() > 0) {
            throw new IllegalArgumentException("invalid slot");
        }
        return new Object[]{this.cart.selectedSlot() + 1};
    }

    @Callback(direct = true, doc = "function([slot:number]):number -- Get the number of items in the specified slot, otherwise in the selected slot.")
    public Object[] count(Context context, Arguments args) {
        int slot = args.optInteger(0, -1);
        int num = 0;
        slot = args.count() > 0 ? slot - 1 : this.cart.selectedSlot();
        if (slot >= 0 && slot < this.cart.getInventorySpace()) {
            if (!this.cart.mainInventory().getStackInSlot(slot).isEmpty()) {
                num = this.cart.mainInventory().getStackInSlot(slot).getCount();
            }
        } else {
            if (args.count() < 1)
                return new Object[]{0, "no slot selected"};
            throw new IllegalArgumentException("invalid slot");
        }
        return new Object[]{num};
    }

    @Callback(direct = true, doc = "function([slot:number]):number -- Get the remaining space in the specified slot, otherwise in the selected slot.")
    public Object[] space(Context context, Arguments args) {
        int slot = args.optInteger(0, -1);
        int num;
        slot = args.count() > 0 ? slot - 1 : this.cart.selectedSlot();
        if (slot > 0 && slot <= this.cart.getInventorySpace()) {
            ItemStack stack = this.cart.mainInventory().getStackInSlot(slot - 1);
            if (!stack.isEmpty()) {
                int maxStack = Math.min(this.cart.mainInventory().getInventoryStackLimit(), stack.getMaxStackSize());
                num = maxStack - stack.getCount();
            } else {
                num = this.cart.mainInventory().getInventoryStackLimit();
            }
        } else {
            if (args.count() < 1)
                return new Object[]{0, "no slot selected"};
            throw new IllegalArgumentException("invalid slot");
        }
        return new Object[]{num};
    }

    @Callback(doc = "function(otherSlot:number):boolean -- Compare the contents of the selected slot to the contents of the specified slot.")
    public Object[] compareTo(Context context, Arguments args) {
        int slotA = args.checkInteger(0) - 1;
        int slotB = this.cart.selectedSlot();
        boolean result;
        if (slotB >= 0 && slotB < this.cart.mainInventory().getSizeInventory())
            return new Object[]{false, "no slot selected"};
        if (slotA >= 0 && slotA < this.cart.mainInventory().getSizeInventory()) {
            ItemStack stackA = this.cart.mainInventory().getStackInSlot(slotA);
            ItemStack stackB = this.cart.mainInventory().getStackInSlot(slotB);
            result = stackA.isEmpty() && stackB.isEmpty() ||
                    !stackA.isEmpty() && !stackB.isEmpty() && stackA.isItemEqual(stackB);
            return new Object[]{result};
        }
        throw new IllegalArgumentException("invalid slot");
    }

    @Callback(doc = "function(toSlot:number[, amount:number]):boolean -- Move up to the specified amount of items from the selected slot into the specified slot.")
    public Object[] transferTo(Context context, Arguments args) {
        int targetSlot = args.checkInteger(0) - 1;
        int amount = args.optInteger(1, this.cart.mainInventory().getInventoryStackLimit());
        if (!(targetSlot >= 0 && targetSlot < this.cart.mainInventory().getSizeInventory()))
            throw new IllegalArgumentException("invalid slot");
        if (!(this.cart.selectedSlot() >= 0 && this.cart.selectedSlot() < this.cart.mainInventory().getSizeInventory()))
            return new Object[]{false, "no slot selected"};

        ItemStack target = this.cart.mainInventory().getStackInSlot(targetSlot);
        ItemStack selected = this.cart.mainInventory().getStackInSlot(this.cart.selectedSlot());
        if ((target.isEmpty() || !selected.isEmpty() && target.isItemEqual(selected)) && !selected.isEmpty()) {
            int items;
            int maxStack = Math.min(this.cart.mainInventory().getInventoryStackLimit(), selected.getMaxStackSize());
            items = maxStack - target.getCount();
            items = Math.min(items, amount);
            if (items <= 0) return new Object[]{false};
            ItemStack dif = this.cart.mainInventory().decrStackSize(this.cart.selectedSlot(), items);
            if (!target.isEmpty()) {
                target.grow(dif.getCount());
            } else {
                this.cart.mainInventory().setInventorySlotContents(targetSlot, dif);
            }
            return new Object[]{true};
        } else {
            return new Object[]{false};
        }
    }

    /*--------Component-Functions-Tank--------*/

    @Callback(doc = "function():number -- The number of tanks installed in the device.")
    public Object[] tankCount(Context context, Arguments args) {
        return new Object[]{this.cart.tanks.tankCount()};
    }

    @Callback(doc = "function([index:number]):number -- Select a tank and/or get the number of the currently selected tank.")
    public Object[] selectTank(Context context, Arguments args) {
        int index = args.optInteger(0, 0);
        if (index > 0 && index <= this.cart.tanks.tankCount())
            this.cart.setSelectedTank(index);
        else if (args.count() > 0)
            throw new IllegalArgumentException("invalid tank index");
        return new Object[]{this.cart.selectedTank()};
    }

    @Callback(direct = true, doc = "function([index:number]):number -- Get the fluid amount in the specified or selected tank.")
    public Object[] tankLevel(Context context, Arguments args) {
        int index = args.optInteger(0, 0);
        index = args.count() > 0 ? index : this.cart.selectedTank();
        if (!(index > 0 && index <= this.cart.tanks.tankCount())) {
            if (args.count() < 1)
                return new Object[]{false, "no tank selected"};
            throw new IllegalArgumentException("invalid tank index");
        }
        return new Object[]{this.cart.tanks.getFluidTank(index).getFluidAmount()};
    }

    @Callback(direct = true, doc = "function([index:number]):number -- Get the remaining fluid capacity in the specified or selected tank.")
    public Object[] tankSpace(Context context, Arguments args) {
        int index = args.optInteger(0, 0);
        index = args.count() > 0 ? index : this.cart.selectedTank();
        if (!(index > 0 && index <= this.cart.tanks.tankCount())) {
            if (args.count() < 1)
                return new Object[]{false, "no tank selected"};
            throw new IllegalArgumentException("invalid tank index");
        }
        IFluidTank tank = this.cart.tanks.getFluidTank(index);
        return new Object[]{tank.getCapacity() - tank.getFluidAmount()};
    }

    @Callback(doc = "function(index:number):boolean -- Compares the fluids in the selected and the specified tank. Returns true if equal.")
    public Object[] compareFluidTo(Context context, Arguments args) {
        int tankA = args.checkInteger(0);
        int tankB = this.cart.selectedTank();
        if (!(tankA > 0 && tankA <= this.cart.tanks.tankCount()))
            throw new IllegalArgumentException("invalid tank index");
        if (!(tankB > 0 && tankB <= this.cart.tanks.tankCount()))
            return new Object[]{false, "no tank selected"};

        FluidStack stackA = this.cart.tanks.getFluidTank(tankA).getFluid();
        FluidStack stackB = this.cart.tanks.getFluidTank(tankB).getFluid();
        boolean res = stackA == null && stackB == null;
        if (!res && stackA != null && stackB != null)
            res = stackA.isFluidEqual(stackB);
        return new Object[]{res};
    }

    @Callback(doc = "function(index:number[, count:number=1000]):boolean -- Move the specified amount of fluid from the selected tank into the specified tank.")
    public Object[] transferFluidTo(Context context, Arguments args) {
        int targetFluidTank = args.checkInteger(0);
        int selectedTank = this.cart.selectedTank();
        int count = args.optInteger(1, 1000);
        if (!(targetFluidTank > 0 && targetFluidTank <= this.cart.tanks.tankCount()))
            throw new IllegalArgumentException("invalid tank index");
        if (!(selectedTank > 0 && selectedTank <= this.cart.tanks.tankCount()))
            return new Object[]{false, "no tank selected"};
        IFluidTank from = this.cart.tanks.getFluidTank(selectedTank);
        IFluidTank to = this.cart.tanks.getFluidTank(targetFluidTank);
        if (to == null) return new Object[]{false, "no tank found"};

        if (from.getFluid() == null || from.getFluid().isFluidEqual(to.getFluid()))
            return new Object[]{false};

        FluidStack sim = from.drain(count, false);    //Simulate the transfer to get the max. moveable amount.
        int move = to.fill(sim, false);

        if (move <= 0) return new Object[]{false};

        FluidStack mv = from.drain(move, true);
        if (mv != null){
            int over = to.fill(mv, true);
            over -= mv.amount;
            if (over > 0) {    //Just in case we drained too much.
                FluidStack ret = mv.copy();
                ret.amount = over;
                from.fill(ret, true);
            }
            return new Object[]{true};
        } else {
            return new Object[]{false};
        }
    }

    //--------World-Inventory----------//

    @Callback(doc = "function(side:string[, count:number=64]):boolean -- Drops items from the selected slot towards the specified side.")
    public Object[] drop(Context context, Arguments args) {
        EnumFacing side = EnumFacing.byName(args.checkString(0));
        if (side == null)
            throw new IllegalArgumentException("invalid side");
        int amount = args.optInteger(1, 64);
        int selectedSlot = this.cart.selectedSlot();
        if (!(selectedSlot >= 0 && selectedSlot < this.cart.mainInventory().getSizeInventory()))
            return new Object[]{false, "no slot selected"};
        if (amount < 1) return new Object[]{false};

        EnumFacing dir = this.cart.toGlobal(side);

        int x = (int) Math.floor(this.cart.xPosition()) + dir.getFrontOffsetX();
        int y = (int) Math.floor(this.cart.yPosition()) + dir.getFrontOffsetY();
        int z = (int) Math.floor(this.cart.zPosition()) + dir.getFrontOffsetZ();

        BlockPos pos = new BlockPos(x, y, z);

        ItemStack selected = this.cart.mainInventory().getStackInSlot(selectedSlot);
        if (selected.isEmpty()) return new Object[]{false};

        if (!(this.cart.world().getTileEntity(new BlockPos(x, y, z)) instanceof IInventory)) {
            List<ItemStack> drop = new ArrayList<>();

            int mov = Math.min(selected.getCount(), amount);
            ItemStack dif = selected.splitStack(mov);
            if (selected.isEmpty())
                this.cart.mainInventory().setInventorySlotContents(selectedSlot, ItemStack.EMPTY);
            drop.add(dif);
            ItemUtil.dropItems(drop, this.cart.world(), x + 0.5, y + 0.5, z + 0.5, false);
            context.pause(Settings.OC_DropDelay);
            return new Object[]{true};
        } else {
            int moved = InventoryUtil.dropItemInventoryWorld(selected.copy(), this.cart.world(), pos, dir.getOpposite(), amount);
            if (moved < 1) return new Object[]{false, "inventory full"};
            if (selected.getCount() > moved) selected.shrink(moved);
            else this.cart.mainInventory().setInventorySlotContents(selectedSlot, ItemStack.EMPTY);
            context.pause(Settings.OC_DropDelay);
            return new Object[]{true};
        }
    }

    @Callback(doc = "function(side:string[, count:number=64]):boolean -- Suck up items from the specified side.")
    public Object[] suck(Context context, Arguments args) {
        EnumFacing side = EnumFacing.byName(args.checkString(0));
        if (side == null)
            throw new IllegalArgumentException("invalid side");
        int amount = args.optInteger(1, 64);
        int sslot = this.cart.selectedSlot();
        if (!(sslot >= 0 && sslot < this.cart.mainInventory().getSizeInventory()))
            return new Object[]{false, "no slot selected"};
        if (amount < 1) return new Object[]{false};

        EnumFacing dir = this.cart.toGlobal(side);
        int x = (int) Math.floor(this.cart.xPosition()) + dir.getFrontOffsetX();
        int y = (int) Math.floor(this.cart.yPosition()) + dir.getFrontOffsetY();
        int z = (int) Math.floor(this.cart.zPosition()) + dir.getFrontOffsetZ();

        BlockPos pos = new BlockPos(x, y, z);

        if (!(this.cart.world().getTileEntity(pos) instanceof IInventory)) {
            int moved = 0;
            int[] acc = InventoryUtil.getAccessible(this.cart.mainInventory(), null);
            acc = InventoryUtil.prioritizeAccessible(acc, this.cart.selectedSlot());
            if (!ItemUtil.hasDroppedItems(this.cart.world(), pos))
                return new Object[]{false};
            for (int i = 0; i < acc.length && moved < 1; i += 1) {
                ItemStack filter = this.cart.mainInventory().getStackInSlot(acc[i]);
                if (!filter.isEmpty()) acc = InventoryUtil.sortAccessible(this.cart.mainInventory(), acc, filter);
                acc = InventoryUtil.prioritizeAccessible(acc, this.cart.selectedSlot());
                int movable = InventoryUtil.spaceForItem(filter, this.cart.mainInventory(), acc);
                movable = Math.min(movable, Math.min(filter.isEmpty() ? 64 : filter.getMaxStackSize(), amount));
                ItemStack stack = ItemUtil.suckItems(this.cart.world(), pos, filter, movable);
                if (!stack.isEmpty()) moved = stack.getCount();
                if (moved > 0)
                    InventoryUtil.putInventory(stack, this.cart.mainInventory(), moved, EnumFacing.UP, acc);
            }
            if (moved > 0) context.pause(Settings.OC_SuckDelay);
            return new Object[]{moved > 0};
        } else {
            int[] mslots = InventoryUtil.getAccessible(this.cart.mainInventory(), EnumFacing.UP);
            int moved = InventoryUtil.suckItemInventoryWorld(this.cart.mainInventory(), mslots, this.cart.selectedSlot(), this.cart.world, pos, dir.getOpposite(), amount);
            if (moved > 0) {
                context.pause(Settings.OC_SuckDelay);
                return new Object[]{true};
            }
            return new Object[]{false};
        }
    }

    //-------World-Tank-------//

    @Callback(doc = "function(side:string):boolean -- Compare the fluid in the selected tank with the fluid on the specified side. Returns true if equal.")
    public Object[] compareFluid(Context context, Arguments args) {
        EnumFacing side = EnumFacing.byName(args.checkString(0));
        if (side == null)
            throw new IllegalArgumentException("invalid side");
        int selectedTank = this.cart.selectedTank();
        if (!(selectedTank > 0 && selectedTank <= this.cart.tanks.tankCount()))
            return new Object[]{false, "no tank selected"};

        EnumFacing dir = this.cart.toGlobal(side);
        int x = (int) Math.floor(this.cart.xPosition()) + dir.getFrontOffsetX();
        int y = (int) Math.floor(this.cart.yPosition()) + dir.getFrontOffsetY();
        int z = (int) Math.floor(this.cart.zPosition()) + dir.getFrontOffsetZ();

        BlockPos pos = new BlockPos(x, y, z);

        IFluidHandler from = TankUtil.getFluidHandler(this.cart.world(), pos, dir.getOpposite());
        FluidStack st = this.cart.tanks.getFluidTank(selectedTank).getFluid();
        if (from == null) return new Object[]{false};
        return new Object[]{TankUtil.hasFluid(from, st)};
    }

    @Callback(doc = "function(side:string[, amount:number=1000]):boolean, number or string -- Drains the specified amount of fluid from the specified side. Returns the amount drained, or an error message.")
    public Object[] drain(Context context, Arguments args) {
        EnumFacing side = EnumFacing.byName(args.checkString(0));
        if (side == null)
            throw new IllegalArgumentException("invalid side");
        int amount = args.optInteger(1, 1000);
        int selectedTank = this.cart.selectedTank();
        if (!(selectedTank > 0 && selectedTank <= this.cart.tanks.tankCount()))
            return new Object[]{false, "no tank selected"};

        EnumFacing dir = this.cart.toGlobal(side);
        int x = (int) Math.floor(this.cart.xPosition()) + dir.getFrontOffsetX();
        int y = (int) Math.floor(this.cart.yPosition()) + dir.getFrontOffsetY();
        int z = (int) Math.floor(this.cart.zPosition()) + dir.getFrontOffsetZ();

        BlockPos pos = new BlockPos(x, y, z);

        IFluidTank from = this.cart.tanks.getFluidTank(selectedTank);
        IFluidHandler to = TankUtil.getFluidHandler(this.cart.world(), pos, dir.getOpposite());
        if (to == null) return new Object[]{false, "no tank found"};

        FluidStack filter = from.getFluid();
        FluidStack drain;
        if (filter == null)
            drain = to.drain(amount, false);
        else
            drain = to.drain(new FluidStack(filter, amount), false);

        if (drain == null) return new Object[]{false, "incompatible or no fluid"};
        int moved = from.fill(drain, false);
        if (moved < 1) return new Object[]{false, "tank full"};

        from.fill(to.drain(new FluidStack(drain, moved), true), true);
        return new Object[]{true, moved};
    }

    @Callback(doc = "function(side:string[, amount:number=1000]):boolean, number or string -- Eject the specified amount of fluid to the specified side. Returns the amount ejected or an error message.")
    public Object[] fill(Context context, Arguments args) {
        EnumFacing side = EnumFacing.byName(args.checkString(0));
        if (side == null)
            throw new IllegalArgumentException("invalid side");
        int amount = args.optInteger(1, 1000);
        int stank = this.cart.selectedTank();
        if (!(stank > 0 && stank <= this.cart.tanks.tankCount()))
            return new Object[]{false, "no tank selected"};

        EnumFacing dir = this.cart.toGlobal(side);
        int x = (int) Math.floor(this.cart.xPosition()) + dir.getFrontOffsetX();
        int y = (int) Math.floor(this.cart.yPosition()) + dir.getFrontOffsetY();
        int z = (int) Math.floor(this.cart.zPosition()) + dir.getFrontOffsetZ();

        BlockPos pos = new BlockPos(x, y, z);

        IFluidTank from = this.cart.tanks.getFluidTank(stank);
        IFluidHandler to = TankUtil.getFluidHandler(this.cart.world(), pos, dir.getOpposite());
        if (to == null) return new Object[]{false, "no tank found"};

        FluidStack drain = from.drain(amount, false);
        if (drain == null)
            return new Object[]{false, "tank is empty"};
        if (TankUtil.getSpaceForFluid(to, drain) < 1)
            return new Object[]{false, "incompatible or no fluid"};
        int moved = to.fill(drain, false);
        if (moved < 1) return new Object[]{false, "no space"};

        to.fill(from.drain(moved, true), true);
        return new Object[]{true, moved};
    }
}
