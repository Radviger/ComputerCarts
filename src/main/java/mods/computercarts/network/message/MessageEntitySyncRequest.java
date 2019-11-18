package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.SyncEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEntitySyncRequest implements IMessage {

    private int entityId;

    public MessageEntitySyncRequest() {}

    public MessageEntitySyncRequest(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.entityId = input.readUnsignedShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<MessageEntitySyncRequest, IMessage> {
        @Override
        public IMessage onMessage(MessageEntitySyncRequest message, MessageContext context) {
            NetHandlerPlayServer netHandler = context.getServerHandler();
            EntityPlayerMP sender = netHandler.player;

            Entity entity = sender.getEntityWorld().getEntityByID(message.entityId);
            if (entity instanceof SyncEntity && entity.getDistanceSq(sender) < 512) { //FIXME Max sync range?
                return new MessageEntitySyncResponse((Entity & SyncEntity)entity);
            }
            return null;
        }

    }

}
