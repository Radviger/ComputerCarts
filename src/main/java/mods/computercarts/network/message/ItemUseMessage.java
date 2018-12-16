package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.items.ItemCartRemoteModule;
import mods.computercarts.common.items.ItemRemoteAnalyzer;
import mods.computercarts.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ItemUseMessage implements IMessage {

    private int id;
    private int pentid;
    private NBTTagCompound data;
    private EnumHand hand;

    public ItemUseMessage() {
    }

    public ItemUseMessage(int id, int pentid, NBTTagCompound data, EnumHand hand) {
        this.id = id;
        this.pentid = pentid;
        this.data = (data == null) ? new NBTTagCompound() : data;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readInt();
        this.pentid = buf.readInt();
        this.data = ByteBufUtils.readTag(buf);
        this.hand = EnumHand.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.id);
        buf.writeInt(this.pentid);
        ByteBufUtils.writeTag(buf, this.data);
        buf.writeByte(this.hand.ordinal());
    }

    public static class Handler implements IMessageHandler<ItemUseMessage, IMessage> {

        @Override
        public IMessage onMessage(ItemUseMessage message, MessageContext ctx) {
            Entity p = Minecraft.getMinecraft().world.getEntityByID(message.pentid);
            if (!(p instanceof EntityPlayer)) return null;
            switch (message.id) {
                case 0:
                    ((ItemCartRemoteModule) ModItems.CART_REMOTE_MODULE).onMPUsage((EntityPlayer) p, message.data, message.hand);
                case 1:
                    ((ItemRemoteAnalyzer) ModItems.CART_REMOTE_ANALYZER).onMPUsage((EntityPlayer) p, message.data, message.hand);
            }
            return null;
        }
    }

}
