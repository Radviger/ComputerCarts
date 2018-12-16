package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.SyncEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EntitySyncRequest implements IMessage {

    protected int enID;
    protected int dimId;

    public EntitySyncRequest() {
    }

    public EntitySyncRequest(Entity entity) {
        enID = entity.getEntityId();
        dimId = entity.world.provider.getDimension();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.enID = buf.readInt();
        this.dimId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.enID);
        buf.writeInt(this.dimId);
    }

    public static class Handler implements IMessageHandler<EntitySyncRequest, IMessage> {

        @Override
        public IMessage onMessage(EntitySyncRequest message, MessageContext ctx) {
            World world = DimensionManager.getWorld(message.dimId);
            if (world != null) {
                Entity entity = world.getEntityByID(message.enID);
                if (entity instanceof SyncEntity) {
                    return new EntitySyncData(entity);
                }
            }
            return null;
        }

    }

}
