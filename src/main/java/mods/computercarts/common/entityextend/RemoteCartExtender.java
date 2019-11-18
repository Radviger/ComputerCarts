package mods.computercarts.common.entityextend;

import com.google.common.base.Charsets;
import li.cil.oc.api.API;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;
import mods.computercarts.ComputerCarts;
import mods.computercarts.common.util.ItemUtil;
import mods.computercarts.common.util.StringUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class RemoteCartExtender implements WirelessEndpoint {

    @CapabilityInject(RemoteCartExtender.class)
    public static Capability<RemoteCartExtender> CAPABILITY = null;

    protected EntityMinecart entity;
    private World world;
    private int posX;
    private int posY;
    private int posZ;
    private boolean stashInit = false;

    private boolean valid = false;
    protected boolean enabled = false;
    protected boolean respbroadcast = true;
    private int nextResp = -1;
    private String nextAddr = null;
    protected int respport = 1;
    protected int cmdport = 2;
    private String address;
    private String owner = null;
    private byte[] password = null;
    private boolean lock = false;
    private ItemStack drop = null;
    private int maxWlanStrength = 4;
    private int curWlanStrength = 4;

    public static RemoteCartExtender getFor(Entity entity, World world) {
        RemoteCartExtender extender = entity.getCapability(CAPABILITY, null);
        if (extender != null) {
            if (!(entity instanceof EntityMinecart)) return null;
            //if(RemoteExtenderRegister.containsEntity(entity.getUniqueID())) return;
            extender.entity = (EntityMinecart) entity;
            extender.world = world;
            extender.valid = true;
            return extender;
        }
        return null;
    }

    @Override
    public int x() {
        return posX;
    }

    @Override
    public int y() {
        return posY;
    }

    @Override
    public int z() {
        return posZ;
    }

    public final void setEnabled(boolean state) {
        this.setEnabled(state, false);
    }

    public final void setEnabled(boolean state, boolean force) {
        if (!valid) return;
        if (this.enabled != state || force) {
            this.enabled = state;
            if (state) {
                if (this.maxWlanStrength > 0) API.network.joinWirelessNetwork(this);
                RemoteExtenderRegister.addRemoteUpdate(this);
            } else {
                API.network.leaveWirelessNetwork(this);
                RemoteExtenderRegister.removeRemoteUpdate(this);
            }
            this.changeEnabled();
        }
    }

    public final boolean isEnabled() {
        return enabled;
    }

    protected void changeEnabled() {
        NBTTagCompound nbt = this.entity.getEntityData();
        nbt.removeTag(ComputerCarts.MODID + ":rc_settings");

        if (isEnabled()) {    //reset some settings
            curWlanStrength = 4;
            address = UUID.randomUUID().toString();
            respport = 1;
            cmdport = 2;
            respbroadcast = true;
            nextResp = -1;
            lock = false;
        }
    }

    protected void processCommand(String cmd, Object[] args) {
        if (cmd == null || cmd.equals("doc")) {
            String a1 = (args.length >= 1 && (args[0] instanceof String)) ? (String) args[0] : null;
            if (a1 != null && this.getCommands().contains(a1)) {
                String doc = this.getDoc(a1);
                doc = (doc == null || Objects.equals(doc, "")) ? "No documentation available" : doc;
                this.sendPacket(new Object[]{doc}, this.getRespPort(), this.getRespAddress());
            } else {
                this.sendPacket(new Object[]{this.getCmdList()}, this.getRespPort(), this.getRespAddress());
            }
        } else if (cmd.equals("response_port")) {
            boolean isValid = args.length >= 1 && (args[0] instanceof Double);
            int port = (isValid) ? (int) (double) (Double) args[0] : 0; //Double is a Class and have to be converted to a double (type)
            port = Math.max(-1, port);
            if (isValid) this.respport = port;
            this.sendPacket(new Object[]{this.respport}, this.getRespPort(), this.getRespAddress());
        } else if (cmd.equals("command_port")) {
            boolean isValid = args.length >= 1 && (args[0] instanceof Double);
            int port = (isValid) ? (int) (double) (Double) args[0] : 0;
            port = Math.max(-1, port);
            if (isValid) this.cmdport = port;
            this.sendPacket(new Object[]{this.cmdport}, this.getRespPort(), this.getRespAddress());
        } else if (cmd.equals("response_broadcast")) {
            boolean isValid = args.length >= 1 && (args[0] instanceof Boolean);
            boolean value = (isValid) ? (Boolean) args[0] : false;
            if (isValid) this.respbroadcast = value;
            this.sendPacket(new Object[]{this.respbroadcast}, this.getRespPort(), this.getRespAddress());
        } else if (cmd.equals("wlan_strength")) {
            int strength = (args.length > 0 && (args[0] instanceof Double)) ? (int) ((double) ((Double) args[0])) : this.curWlanStrength;    //Double (object) -> double (number) -> int
            strength = Math.min(strength, this.maxWlanStrength);
            this.curWlanStrength = strength;
            this.sendPacket(new Object[]{strength, this.maxWlanStrength}, this.getRespPort(), this.getRespAddress());
        }

    }

    private String getCmdList() {
        Iterator<String> it = this.getCommands().iterator();
        StringBuilder st = new StringBuilder("{");
        while (it.hasNext()) {
            st.append(it.next()).append((it.hasNext()) ? "," : "");
        }
        st.append("}");
        return st.toString();
    }

    protected final int getRespPort() {
        if (this.respport >= 0) return this.respport;
        return this.nextResp;
    }

    protected final String getRespAddress() {
        if (this.respbroadcast) return null;
        return this.nextAddr;
    }

    protected final void sendPacket(Object[] msg, int port, String des) {
        if (this.getRespPort() < 0) return;
        Packet packet = API.network.newPacket(address, des, port, msg);
        API.network.sendWirelessPacket(this, this.curWlanStrength, packet);
    }

    protected List<String> getCommands() {
        List<String> cmd = new ArrayList<>();
        cmd.add("doc");
        cmd.add("response_port");
        cmd.add("command_port");
        cmd.add("response_broadcast");
        cmd.add("wlan_strength");
        return cmd;
    }

    protected String getDoc(String cmd) {
        if (cmd == null) return null;
        switch (cmd) {
            case "doc":
                return "doc([func:string]):table or string -- get a list of functions or a documentation for a function.";
            case "response_port":
                return "response_port([port:number]):number -- sets the response port and returns the new port. -1 to response on the same port as the last message";
            case "command_port":
                return "command_port([port:number]):number -- sets the command port and returns the new port. -1 to accept all ports";
            case "response_broadcast":
                return "response_broadcast([value:boolean]):boolean -- if the value is true it will respond with private messages.";
            case "wlan_strength":
                return "wlan_strength([value:number]):number,number -- get/set the current and get the max. wireless strength.";
        }
        return null;
    }

    public void update() {
        if (this.world().isRemote) return;
        boolean hasEntity = world.loadedEntityList.contains(this.entity);
        boolean chunkLoaded = world.getChunk((int) entity.posX >> 4, (int) entity.posZ >> 4).isLoaded();
        if (this.entity.isDead || this.entity.getDamage() >= this.getMaxModuleDamage()) {
            this.setEnabled(false, true);
            if (this.entity.getDamage() >= this.getMaxModuleDamage()) {
                this.dropItem();
            }
        } else {
            if (!chunkLoaded || !hasEntity) {
                /*
                 * Stop the updates on chunk unload.
                 * A chunk reload should reinitialize the entity and reactivate the updates.
                 */
                RemoteExtenderRegister.removeRemoteUpdate(this);
            } else if (stashInit) {
                stashInit = false;
                if (this.maxWlanStrength > 0) API.network.joinWirelessNetwork(this);
            }
            this.posX = (int) entity.posX;
            this.posY = (int) entity.posY;
            this.posZ = (int) entity.posZ;
            if (this.maxWlanStrength > 0) API.network.updateWirelessNetwork(this);
        }
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public void receivePacket(Packet packet, WirelessEndpoint sender) {
        if (packet.ttl() < 0 || !inRange(sender, curWlanStrength)) return;
        if (!(packet.destination() == null || packet.destination().equals(this.address)) || !(this.cmdport == -1 || packet.port() == this.cmdport))
            return;
        if (!(packet.data()[0] instanceof byte[]) || ((packet.data().length < 2 || !(packet.data()[1] instanceof byte[])) && this.hasPassword()))
            return;

        boolean usePassword = false;
        if (this.hasPassword()) {
            String passw = new String((byte[]) packet.data()[1], Charsets.UTF_8);
            if (passw.length() <= 2 || !passw.substring(0, 2).equals("::")
                    || !this.isCorrectPassword(passw.substring(2))) return;
            usePassword = true;
        } else if (packet.data().length > 2 && packet.data()[1] instanceof byte[]) {
            String passw = new String((byte[]) packet.data()[1], Charsets.UTF_8);
            if (passw.length() >= 2 && passw.substring(0, 2).equals("::"))
                usePassword = true;
        }

        this.nextAddr = packet.source();
        this.nextResp = packet.port();

        String cmd = new String((byte[]) packet.data()[0], Charsets.UTF_8);
        Object[] data = (this.getCommands().contains(cmd)) ? this.processPacket(packet.data(), usePassword) : new Object[]{};
        this.processCommand((this.getCommands().contains(cmd)) ? cmd : null, data);
    }

    private Object[] processPacket(Object[] data, boolean hasPassword) {
        int dataoffset = (hasPassword) ? 2 : 1;
        if (data.length - dataoffset < 1) return new Object[]{};
        Object[] res = new Object[data.length - dataoffset];
        for (int i = dataoffset; i < data.length; i += 1) {
            if (data[i] instanceof byte[]) {
                res[i - dataoffset] = new String((byte[]) data[i], Charsets.UTF_8);
            } else {
                res[i - dataoffset] = data[i];
            }
        }
        return res;
    }

    public NBTTagCompound saveNBTData(NBTTagCompound output) {
        output.setBoolean(ComputerCarts.MODID + ":rc_enabled", enabled);
        if (enabled) {
            NBTTagCompound rc = new NBTTagCompound();
            if (this.address != null) rc.setString("rc_address", this.address);
            rc.setBoolean("rc_respbroadcast", this.respbroadcast);
            rc.setInteger("rc_respport", this.respport);
            rc.setInteger("rc_cmdport", this.cmdport);
            if (password != null) rc.setByteArray("rc_password", this.password);
            rc.setInteger("rc_maxwlan", this.maxWlanStrength);
            rc.setInteger("rc_curwlan", this.curWlanStrength);
            if (owner != null) rc.setString("rc_owner", this.owner);
            else rc.removeTag("rc_owner");
            rc.setBoolean("rc_locked", this.lock);
            if (drop != null) {
                NBTTagCompound dropnbt = new NBTTagCompound();
                drop.writeToNBT(dropnbt);
                rc.setTag("rc_dropitem", dropnbt);
            }
            this.writeModuleNBT(rc);
            output.setTag(ComputerCarts.MODID + ":rc_settings", rc);
        }
        return output;
    }

    public void loadNBTData(NBTTagCompound input) {
        //boolean hasEntity = world.loadedEntityList.contains(this.entity);
        //if(!hasEntity && this.world != entity.world) return;


        if (input.hasKey(ComputerCarts.MODID + ":rc_enabled"))
            enabled = input.getBoolean(ComputerCarts.MODID + ":rc_enabled");
        else
            enabled = false;

        if (input.hasKey(ComputerCarts.MODID + ":rc_settings") && this.enabled) {
            NBTTagCompound rc = input.getCompoundTag(ComputerCarts.MODID + ":rc_settings");
            if (rc.hasKey("rc_address")) this.address = rc.getString("rc_address");
            else if (rc.hasKey("rc_uuid")) this.address = rc.getString("rc_uuid");

            if (rc.hasKey("rc_respbroadcast")) this.respbroadcast = rc.getBoolean("rc_respbroadcast");
            if (rc.hasKey("rc_respport")) this.respport = rc.getInteger("rc_respport");
            if (rc.hasKey("rc_cmdport")) this.cmdport = rc.getInteger("rc_cmdport");
            if (rc.hasKey("rc_password")) this.password = rc.getByteArray("rc_password");
            if (rc.hasKey("rc_maxwlan")) this.maxWlanStrength = rc.getInteger("rc_maxwlan");
            if (rc.hasKey("rc_curwlan")) this.curWlanStrength = rc.getInteger("rc_curwlan");
            if (rc.hasKey("rc_dropitem")) {
                NBTTagCompound dropnbt = rc.getCompoundTag("rc_dropitem");
                drop = new ItemStack(dropnbt);
            }
            if (rc.hasKey("rc_owner")) this.owner = rc.getString("rc_owner");
            if (rc.hasKey("rc_locked")) this.lock = rc.getBoolean("rc_locked");

            this.loadModuleNBT(rc);
        }

        if (this.enabled) {
            stashInit = true;
            boolean c = RemoteExtenderRegister.addRemoteUpdate(this);
            if (!c) {
                RemoteExtenderRegister.removeRemoteUpdate(this.entity); //if it failed remove existing Remotes from the entity
                RemoteExtenderRegister.addRemoteUpdate(this);
            }
        } else {
            RemoteExtenderRegister.removeRemoteUpdate(this);    // Just to make sure nothing bad happens
        }
    }

    public boolean inRange(WirelessEndpoint w, double range) {
        int x = this.x() - w.x();
        int y = this.y() - w.y();
        int z = this.z() - w.z();
        return (x * x) + (y * y) + (z * z) <= (range * range);
    }

    public String getAddress() {
        return (this.enabled) ? this.address : null;
    }

    protected void loadModuleNBT(NBTTagCompound nbt) {
    }

    protected void writeModuleNBT(NBTTagCompound nbt) {
    }

    public void onAnalyzeModule(EntityPlayer p) {
        p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Address: " + TextFormatting.RESET + this.address));
        p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Response Port: " + TextFormatting.RESET + this.respport));
        p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Command Port: " + TextFormatting.RESET + this.cmdport));
        p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Boradcast Response: " + TextFormatting.RESET + this.respbroadcast));
        p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Wireless Strength: " + TextFormatting.RESET + this.curWlanStrength + " / " + this.maxWlanStrength));
    }

    public void dropItem() {
        if (this.drop != null)
            ItemUtil.dropItem(this.drop, this.entity.world, this.entity.posX, this.entity.posY, this.entity.posZ, true);
    }

    public boolean editableByPlayer(EntityPlayer p, boolean noPublic) {
        return (!this.lock && !noPublic) || this.owner == null || p.getUniqueID().toString().equals(this.owner)
                || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(p.getGameProfile());
    }

    public EntityMinecart getCart() {
        return this.entity;
    }

    //Max damage the cart/locomotive can have.
    public int getMaxModuleDamage() {
        return 40;
    }

    public ItemStack getRemoteItem() {
        return this.drop;
    }

    public void setRemoteItem(ItemStack drop) {
        if (drop != null) {
            this.drop = drop.copy();
            this.drop.setCount(1);
        } else this.drop = null;
    }

    public int getMaxWlanStrength() {
        return maxWlanStrength;
    }

    public void setMaxWlanStrength(int maxWlanStrength) {
        if (this.maxWlanStrength >= 1 && maxWlanStrength < 1 && this.enabled)
            API.network.leaveWirelessNetwork(this);
        else if (this.maxWlanStrength < 1 && maxWlanStrength >= 1 && this.enabled)
            API.network.joinWirelessNetwork(this);
        this.maxWlanStrength = maxWlanStrength;
        this.curWlanStrength = Math.min(this.curWlanStrength, this.maxWlanStrength);
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 1) {
            this.password = null;
            return;
        }
        this.password = StringUtil.getMD5Array(password);
    }

    public boolean hasPassword() {
        return this.password != null;
    }

    public boolean isCorrectPassword(String password) {
        if (this.password == null) return true;
        byte[] pass = StringUtil.getMD5Array(password);
        String hex1 = StringUtil.byteToHex(this.password);
        String hex2 = StringUtil.byteToHex(pass);
        return hex1.equals(hex2);
    }

    public int getCurWlanStrength() {
        return curWlanStrength;
    }

    public void setCurWlanStrength(int curWlanStrength) {
        this.curWlanStrength = Math.min(curWlanStrength, maxWlanStrength);
    }

    public void setOwner(String uuid) {
        this.owner = uuid;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setLocked(boolean lock) {
        this.lock = lock;
    }

    public boolean isLocked() {
        return this.lock;
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private RemoteCartExtender extender = CAPABILITY.getDefaultInstance();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing enumFacing) {
            return capability == CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing enumFacing) {
            return capability == CAPABILITY ? CAPABILITY.cast(this.extender) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound) getStorage().writeNBT(CAPABILITY, this.extender, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound input) {
            getStorage().readNBT(CAPABILITY, this.extender, null, input);
        }

        public Storage getStorage() {
            return (Storage) CAPABILITY.getStorage();
        }
    }

    public static class Storage implements Capability.IStorage<RemoteCartExtender> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<RemoteCartExtender> capability, RemoteCartExtender extender, EnumFacing enumFacing) {
            return extender.saveNBTData(new NBTTagCompound());
        }

        @Override
        public void readNBT(Capability<RemoteCartExtender> capability, RemoteCartExtender extender, EnumFacing enumFacing, NBTBase nbt) {
            extender.loadNBTData((NBTTagCompound) nbt);
        }
    }
}
