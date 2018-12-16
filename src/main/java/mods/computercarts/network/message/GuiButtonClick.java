package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.container.RemoteModuleContainer;
import mods.computercarts.common.entityextend.RemoteCartExtender;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

//Message from Client to send Server a GuiButton event if the Inventory is a Entity
public class GuiButtonClick implements IMessage {

    public int uiID;
    public int buttonid;
    public NBTTagCompound dat;

    public GuiButtonClick() {
    }

    public static GuiButtonClick entityButtonClick(Entity e, int button, int guiID) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("en", e.getEntityId());
        data.setInteger("dim", e.world.provider.getDimension());
        return new GuiButtonClick(guiID, button, data);
    }

    public static GuiButtonClick tileButtonClick(TileEntity entity, int button, int guiID) {
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("pos", entity.getPos().toLong());
        data.setInteger("dim", entity.getWorld().provider.getDimension());
        return new GuiButtonClick(guiID, button, data);
    }

    public GuiButtonClick(int guiID, int button, NBTTagCompound data) {
        uiID = guiID;
        buttonid = button;
        dat = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uiID = buf.readInt();
        buttonid = buf.readInt();
        if (buf.isReadable())
            dat = ByteBufUtils.readTag(buf);
        if (dat == null) dat = new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(uiID);
        buf.writeInt(buttonid);
        if (dat != null) ByteBufUtils.writeTag(buf, dat);
    }

    public static class Handler implements IMessageHandler<GuiButtonClick, IMessage> {

        @Override
        public IMessage onMessage(GuiButtonClick message, MessageContext ctx) {
            switch (message.uiID) {
                case 0:
                    TileEntity tile = DimensionManager.getWorld(message.dat.getInteger("dim")).getTileEntity(BlockPos.fromLong(message.dat.getLong("pos")));
                    if (tile != null) {
                        if (tile instanceof TileEntityNetworkRailBase)
                            ((TileEntityNetworkRailBase) tile).onButtonPress(message.buttonid);
                    }
                    break;
                case 1:
                    Entity entity = DimensionManager.getWorld(message.dat.getInteger("dim")).getEntityByID(message.dat.getInteger("en"));
                    if (entity != null) {
                        if (entity instanceof EntityComputerCart && message.buttonid == 0)
                            ((EntityComputerCart) entity).setRunning(!((EntityComputerCart) entity).getRunning());
                    }
                    break;
                case 2:
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (player == null) break;
                    Container c = player.openContainer;
                    if (c instanceof RemoteModuleContainer) {
                        if (message.buttonid == 0) {
                            RemoteCartExtender module = ((RemoteModuleContainer) c).getModule();
                            String pw = message.dat.getString("password");
                            int stat = module.editableByPlayer(player, true) ? 1 : 2;
                            if (pw.length() > 10) stat = 2;
                            if (stat == 1) module.setPassword(pw);
                            ((RemoteModuleContainer) c).sendPassState(player, stat);
                        } else if (message.buttonid == 1) {
                            RemoteCartExtender module = ((RemoteModuleContainer) c).getModule();
                            if (!module.editableByPlayer(player, true)) break;
                            module.setLocked(!module.isLocked());
                            if (module.isLocked()) ((RemoteModuleContainer) c).lockGui();
                        } else if (message.buttonid == 2) {
                            RemoteCartExtender module = ((RemoteModuleContainer) c).getModule();
                            if (!module.editableByPlayer(player, false)) break;
                            module.setEnabled(false);
                            if (module.getRemoteItem() != null && !player.inventory.addItemStackToInventory(module.getRemoteItem()))
                                module.dropItem();
                        }
                    }
                    break;
            }
            return null;
        }

    }
}
