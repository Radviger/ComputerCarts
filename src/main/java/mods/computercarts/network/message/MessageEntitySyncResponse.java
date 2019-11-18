package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.SyncEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEntitySyncResponse implements IMessage {

    private int entityId;
    private NBTTagCompound data;

    public MessageEntitySyncResponse() {}

    public <E extends Entity & SyncEntity> MessageEntitySyncResponse(E entity) {
        entityId = entity.getEntityId();
        data = entity.writeSyncData(new NBTTagCompound());
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.entityId = input.readInt();
        this.data = ByteBufUtils.readTag(input);
    }

    @Override
    public void toBytes(ByteBuf output) {
        output.writeInt(this.entityId);
        ByteBufUtils.writeTag(output, data);
    }

    public static class Handler implements IMessageHandler<MessageEntitySyncResponse, IMessage> {
        @Override
        public IMessage onMessage(MessageEntitySyncResponse message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().player.world;
            if (world != null) {
                Entity entity = world.getEntityByID(message.entityId);
                if (entity instanceof SyncEntity) {
                    ((SyncEntity) entity).readSyncData(message.data);
                }
            }
            return null;
        }
    }
}
