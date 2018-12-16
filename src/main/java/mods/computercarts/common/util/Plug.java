package mods.computercarts.common.util;

import li.cil.oc.api.Persistable;
import li.cil.oc.api.network.*;
import net.minecraft.nbt.NBTTagCompound;

public class Plug implements Environment, Persistable {

    private Plugable master;
    private Node node;

    public Plug(Plugable master) {
        this.master = master;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public Node node() {
        return this.node;
    }

    @Override
    public void onConnect(Node node) {
        master.onPlugConnect(this, node);
    }

    @Override
    public void onDisconnect(Node node) {
        master.onPlugDisconnect(this, node);
    }

    @Override
    public void onMessage(Message message) {
        master.onPlugMessage(this, message);
    }

    public Plugable getMaster() {
        return this.master;
    }

    @Override
    public void load(NBTTagCompound nbt) {
        NBTTagCompound ntag = nbt.getCompoundTag("node");

        if (this.node instanceof ComponentConnector) this.node.load(nbt);
        else if (this.node instanceof Connector) this.node.load(nbt);
        else if (this.node instanceof Component) this.node.load(nbt);
        else this.node.save(ntag);

    }

    @Override
    public void save(NBTTagCompound nbt) {
        NBTTagCompound ntag = new NBTTagCompound();
        if (this.node instanceof ComponentConnector) this.node.save(nbt);
        else if (this.node instanceof Connector) this.node.save(nbt);
        else if (this.node instanceof Component) this.node.save(nbt);
        else this.node.save(ntag);
        nbt.setTag("node", ntag);
    }
}
