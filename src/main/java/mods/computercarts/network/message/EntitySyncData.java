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

public class EntitySyncData implements IMessage {

    protected int enID;
    protected int dimId;
    protected NBTTagCompound nbt;

    public EntitySyncData() {
    }

    public EntitySyncData(Entity entity) {
        enID = entity.getEntityId();
        dimId = entity.world.provider.getDimension();
        if (entity instanceof SyncEntity) {
            nbt = new NBTTagCompound();
            ((SyncEntity) entity).writeSyncData(nbt);
        } else nbt = null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.enID = buf.readInt();
        this.dimId = buf.readInt();
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.enID);
        buf.writeInt(this.dimId);
        ByteBufUtils.writeTag(buf, nbt);
    }

    public static class Handler implements IMessageHandler<EntitySyncData, IMessage> {

        @Override
        public IMessage onMessage(EntitySyncData message, MessageContext ctx) {
            if (message.nbt != null) {    //If the nbt is null then the entity is not syncable and we dosn't need to handle this
                World world = Minecraft.getMinecraft().player.world;
                if (world != null && world.provider.getDimension() == message.dimId) { // just to make sure that the player has not moved to an other dimension
                    Entity entity = world.getEntityByID(message.enID);
                    if (entity instanceof SyncEntity) {
                        ((SyncEntity) entity).readSyncData(message.nbt);
                    }
                }
            }
            return null;
        }


    }

}
