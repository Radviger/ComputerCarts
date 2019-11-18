package mods.computercarts.common.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.*;
import mods.computercarts.Settings;
import mods.computercarts.common.util.Plug;
import mods.computercarts.common.util.Plugable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.Objects;

import static mods.computercarts.ComputerCarts.MODID;

//@Optional.Interface(iface = "appeng.api.movable.IMovableTile", modid = "appliedenergistics2", striprefs = true)
public class TileEntityNetworkRailController extends TileEntity implements SidedEnvironment, Plugable, Analyzable, ITickable {


    private Plug rail;    //Environment for the Cart
    private Plug side;    //Environment for Cables, Computers, ...

    private boolean setup = true; //First call of updateEntity()

    private ConnectionMode mode = ConnectionMode.NETWORK_AND_POWER;
    private boolean moving = false;

    public TileEntityNetworkRailController() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            rail = new Plug(this);
            side = new Plug(this);

            rail.setNode(Network.newNode(rail, Visibility.Network).withConnector().create());
            side.setNode(Network.newNode(side, Visibility.Network).withConnector(500D).create());
        }
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound input) {
        super.readFromNBT(input);
        if (input.hasKey("conMode")) mode = ConnectionMode.values()[input.getInteger("conMode")];

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            if (input.hasKey("Plug_1")) side.load((NBTTagCompound) input.getTag("Plug_1"));
            if (input.hasKey("Plug_2")) rail.load((NBTTagCompound) input.getTag("Plug_2"));
        }

        this.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound output) {
        output = super.writeToNBT(output);
        output.setInteger("conMode", mode.ordinal());

        if (!this.world.isRemote) {
            NBTTagCompound plug1 = new NBTTagCompound();
            NBTTagCompound plug2 = new NBTTagCompound();

            side.save(plug1);
            output.setTag("Plug_1", plug1);

            rail.save(plug2);
            output.setTag("Plug_2", plug2);
        }

        return output;
    }

    private void forceSync() {
        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
        this.markDirty();
    }

    @Override
    public void update() {
        if (!this.world.isRemote && !this.moving) {
            if (setup) {
                Network.joinOrCreateNetwork(this);
                Network.joinNewNetwork(this.rail.node());
                setup = false;
            }

            if (mode.isPower()) {
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

    public String getName() {
        return "gui." + MODID + ".network_rail_controller.title";
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64;
    }

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
        if (mode.isNetwork()) {
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

    public ConnectionMode getMode() {
        return this.mode;
    }

    /*-----END-OC-Network----*/

    public void onButtonPress(int button) {
        if (button == 0) {
            this.mode = this.mode.next();
        }
    }

    public void setMode(ConnectionMode Mode) {
        this.mode = Mode;
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

    public enum ConnectionMode {
        NETWORK_AND_POWER("network_power"),
        NETWORK("network"),
        POWER("power"),
        NONE("none");

        private final String name;

        ConnectionMode(String name) {
            this.name = name;
        }

        public boolean isPower() {
            return this == NETWORK_AND_POWER || this == POWER;
        }

        public boolean isNetwork() {
            return this == NETWORK_AND_POWER || this == NETWORK;
        }

        public ConnectionMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public String getName() {
            return "gui." + MODID +".network_rail_controller.mode." + this.name;
        }
    }
}