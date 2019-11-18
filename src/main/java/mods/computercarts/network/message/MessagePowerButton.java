package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePowerButton implements IMessage {
    private int entityId;

    public MessagePowerButton() {}

    public MessagePowerButton(EntityComputerCart cart) {
        this.entityId = cart.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.entityId = input.readUnsignedShort();
    }

    @Override
    public void toBytes(ByteBuf output) {
        output.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<MessagePowerButton, IMessage> {
        @Override
        public IMessage onMessage(MessagePowerButton message, MessageContext context) {
            NetHandlerPlayServer netHandler = context.getServerHandler();
            EntityPlayerMP sender = netHandler.player;

            Entity entity = sender.getEntityWorld().getEntityByID(message.entityId);
            if (entity instanceof EntityComputerCart && entity.getDistanceSq(sender) <= 64) {
                EntityComputerCart cart = (EntityComputerCart) entity;
                cart.setRunning(!cart.isRunning());
            }
            return null;
        }
    }
}
