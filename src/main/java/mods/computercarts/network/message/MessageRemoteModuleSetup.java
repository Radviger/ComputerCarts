package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.container.ContainerRemoteModule;
import mods.computercarts.common.entityextend.RemoteCartExtender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRemoteModuleSetup implements IMessage {

    private Action action;
    private String password;

    public MessageRemoteModuleSetup() {}

    public MessageRemoteModuleSetup(Action action, String password) {

        this.action = action;
        this.password = password;
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.action = Action.values()[input.readByte()];
        if (this.action == Action.SET_PASSWORD) {
            this.password = ByteBufUtils.readUTF8String(input);
        }
    }

    @Override
    public void toBytes(ByteBuf output) {
        output.writeByte(this.action.ordinal());
        if (this.action == Action.SET_PASSWORD) {
            ByteBufUtils.writeUTF8String(output, this.password);
        }
    }

    public enum Action {
        SET_PASSWORD, LOCK, DISABLE
    }

    public static class Handler implements IMessageHandler<MessageRemoteModuleSetup, IMessage> {

        @Override
        public IMessage onMessage(MessageRemoteModuleSetup message, MessageContext ctx) {
            NetHandlerPlayServer netHandler = ctx.getServerHandler();
            EntityPlayerMP sender = netHandler.player;

            if (sender.openContainer instanceof ContainerRemoteModule) {
                ContainerRemoteModule c = (ContainerRemoteModule) sender.openContainer;
                switch (message.action) {
                    case SET_PASSWORD: {
                        RemoteCartExtender module = c.getModule();
                        String pw = message.password;
                        int stat = module.editableByPlayer(sender, true) ? 1 : 2;
                        if (pw.length() > 10) stat = 2;
                        if (stat == 1) module.setPassword(pw);
                        c.sendPassState(sender, stat);
                        break;
                    }
                    case LOCK: {
                        RemoteCartExtender module = c.getModule();
                        if (module.editableByPlayer(sender, true)) {
                            module.setLocked(!module.isLocked());
                            if (module.isLocked()) c.lockGui();
                        }
                        break;
                    }
                    case DISABLE: {
                        RemoteCartExtender module = c.getModule();
                        if (module.editableByPlayer(sender, false)) {
                            module.setEnabled(false);
                            if (module.getRemoteItem() != null && !sender.inventory.addItemStackToInventory(module.getRemoteItem()))
                                module.dropItem();
                        }
                        break;
                    }
                }
            }
            return null;
        }

    }
}
