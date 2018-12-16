package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ComputercartInventoryUpdate implements IMessage {

    private int entityid;
    private int dimid;
    private int slot;
    private ItemStack stack;

    public ComputercartInventoryUpdate() {
    }

    public ComputercartInventoryUpdate(EntityComputerCart cart, int slot, ItemStack stack) {
        this.entityid = cart.getEntityId();
        this.dimid = cart.world.provider.getDimension();
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityid = buf.readInt();
        this.dimid = buf.readInt();
        this.slot = buf.readInt();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityid);
        buf.writeInt(this.dimid);
        buf.writeInt(this.slot);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<ComputercartInventoryUpdate, IMessage> {

        @Override
        public IMessage onMessage(ComputercartInventoryUpdate message, MessageContext ctx) {
            World w = Minecraft.getMinecraft().player.world;
            if (w != null && w.provider.getDimension() == message.dimid) {
                Entity e = w.getEntityByID(message.entityid);
                if (e instanceof EntityComputerCart) {
                    ((EntityComputerCart) e).compinv.setInventorySlotContents(message.slot, message.stack);
                }
            }
            return null;
        }

    }

}
