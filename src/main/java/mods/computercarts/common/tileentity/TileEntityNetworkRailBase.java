package mods.computercarts.common.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.*;
import mods.computercarts.ComputerCarts;
import mods.computercarts.Settings;
import mods.computercarts.common.util.Plug;
import mods.computercarts.common.util.Plugable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

//@Optional.Interface(iface = "appeng.api.movable.IMovableTile", modid = "appliedenergistics2", striprefs = true)
public class TileEntityNetworkRailBase extends TileEntity implements IInventory, SidedEnvironment, Plugable, Analyzable, ITickable {


    private Plug rail;    //Environment for the Cart
    private Plug side;    //Environment for Cables, Computers, ...

    private boolean firstupdate = true; //First call of updateEntity()

    @Nonnull
    private ItemStack camoItem = ItemStack.EMPTY;
    @Nonnull
    private ItemStack camoItemOld = ItemStack.EMPTY;

    /*
     * 0 = Connect: Network,Power
     * 1 = Connect: Network
     * 2 = Connect: Power
     */
    private int Mode = 0;
    private boolean moving = false;

    public TileEntityNetworkRailBase() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            rail = new Plug(this);
            side = new Plug(this);

            rail.setNode(Network.newNode(rail, Visibility.Network).withConnector().create());
            side.setNode(Network.newNode(side, Visibility.Network).withConnector(500D).create());
        }
        this.markDirty();
    }

    /*---------NBT/Sync--------*/
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("conMode")) Mode = compound.getInteger("conMode");

        camoItem = new ItemStack((NBTTagCompound) compound.getTag("CamoItem"));

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            if (compound.hasKey("Plug_1")) side.load((NBTTagCompound) compound.getTag("Plug_1"));
            if (compound.hasKey("Plug_2")) rail.load((NBTTagCompound) compound.getTag("Plug_2"));
        }

        this.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setInteger("conMode", Mode);

        if (!this.world.isRemote) {
            NBTTagCompound plug1 = new NBTTagCompound();
            NBTTagCompound plug2 = new NBTTagCompound();

            side.save(plug1);
            compound.setTag("Plug_1", plug1);

            rail.save(plug2);
            compound.setTag("Plug_2", plug2);
        }

        NBTTagCompound item = new NBTTagCompound();
        if (!camoItem.isEmpty()) camoItem.writeToNBT(item);
        compound.setTag("CamoItem", item);
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound data = new NBTTagCompound();
        this.writeSyncableDataToNBT(data);
        return new SPacketUpdateTileEntity(this.pos, 1, data);
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
        this.readSyncableDataFromNBT(packet.getNbtCompound());
    }

    private void writeSyncableDataToNBT(NBTTagCompound compound) {
        NBTTagCompound item = new NBTTagCompound();
        if (!camoItem.isEmpty()) camoItem.writeToNBT(item);
        compound.setTag("CamoItem", item);
    }

    private void readSyncableDataFromNBT(NBTTagCompound compound) {
        camoItem = new ItemStack((NBTTagCompound) compound.getTag("CamoItem"));
        if (camoItem != camoItemOld) updateCamouflage();
    }

    private void forceSync() {
        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
        this.markDirty();
    }

    @Override
    public void update() {
        if (!this.world.isRemote && !this.moving) {
            if (firstupdate) {
                Network.joinOrCreateNetwork(this);
                Network.joinNewNetwork(this.rail.node());
                firstupdate = false;
            }

            if (Mode == 0 || Mode == 2) {
                Connector con1 = (Connector) side.node();
                Connector con2 = (Connector) rail.node();
                if (con2.globalBuffer() < con2.globalBufferSize()) {
                    double need = Math.min(con2.globalBufferSize() - con2.globalBuffer(), Settings.NetRailPowerTransfer);
                    double provide = need + con1.changeBuffer(-need);
                    con2.changeBuffer(provide);
                }
            }
        }
    }
    /*------END-NBT/Sync------*/

    /*------Tile-Update-------*/

    private void updateCamouflage() {
        if (this.world.isRemote) {
            camoItemOld = camoItem.copy();
            if (!camoItem.isEmpty()) {
                if (camoItem.getItem() instanceof ItemBlock) {
                    //...
                } else {
                    //...
                }
            } else {
                //...
            }
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (rail != null) {
            rail.node().remove();
        }
        if (side != null) {
            side.node().remove();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (rail != null) {
            rail.node().remove();
        }
        if (side != null) {
            side.node().remove();
        }
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return camoItem.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0) return camoItem;
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (!camoItem.isEmpty() && slot == 0) {
            ItemStack stack;

            if (camoItem.getCount() <= amount) {
                stack = camoItem;
                camoItem = ItemStack.EMPTY;
                this.forceSync();
                return stack;
            } else {
                stack = camoItem.splitStack(amount);

                if (camoItem.getCount() <= 0) camoItem = ItemStack.EMPTY;
                this.forceSync();
                return stack;
            }
        }
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        if (slot == 0) {
            ItemStack result = this.camoItem;
            this.camoItem = ItemStack.EMPTY;
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack item) {
        if (slot == 0 && !item.isEmpty()) {
            camoItem = item.copy();


            if (item.getCount() > this.getInventoryStackLimit()) {
                item.setCount(this.getInventoryStackLimit());
            }
            this.forceSync();
        }
    }

    @Override
    public String getName() {
        return "gui." + ComputerCarts.MODID + ".network_rail_base.title";
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) {
            if (stack.getItem() instanceof ItemBlock) {
                Block block = Block.getBlockFromItem(stack.getItem());
                return block.getRenderType(block.getDefaultState()) == EnumBlockRenderType.MODEL;
            }

            return false;
        }
        return false;
    }

    @Override
    public int getField(int i) {
        return 0;
    }

    @Override
    public void setField(int i, int i1) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.camoItem = ItemStack.EMPTY;
    }

    /*-----OC-Network------*/

    @Override
    public Node sidedNode(EnumFacing side) {
        if (this.world != null && !this.world.isRemote && side != null && !side.equals(EnumFacing.UP))
            return this.side.node();
        return null;
    }

    @Override
    public boolean canConnect(EnumFacing side) {
        return side != null && !side.equals(EnumFacing.UP);
    }

    @Override
    public void onPlugMessage(Plug plug, Message message) {
        if (Mode == 0 || Mode == 1) {
            if (Objects.equals(message.name(), "network.message") && this.side.node() != message.source() && this.rail.node() != message.source()) {
                if (plug == rail) side.node().sendToReachable("network.message", message.data());
                else if (plug == side) rail.node().sendToReachable("network.message", message.data());
            }
        }
    }

    @Override
    public void onPlugConnect(Plug plug, Node node) {}

    @Override
    public void onPlugDisconnect(Plug plug, Node node) {
        if (plug == this.rail && rail.node().network() == null) Network.joinNewNetwork(this.rail.node());
    }

    public int getMode() {
        return this.Mode;
    }

    /*-----END-OC-Network----*/

    public void onButtonPress(int buttonID) {
        if (buttonID == 0) {
            this.Mode += 1;
            if (this.Mode > 3) this.Mode = 0;
        }
    }

    public void setMode(int Mode) {
        this.Mode = Mode;
    }

    @Override
    public Node[] onAnalyze(EntityPlayer player, EnumFacing side, float v, float v1, float v2) {
        return null;
    }

    public Environment getRailPlug() {
        return this.rail;
    }

    /*-------AE2-Spatial-Storage-Handler------*/

    /*public void doneMoving() {
        this.moving = false;
        Network.joinOrCreateNetwork(this);
        this.forceSync();
    }

    public boolean prepareToMove() {
        this.moving = true;
        return true;
    }*/

    /*-------END-AE2-Spatial-Storage-Handler------*/


}