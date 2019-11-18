package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.ComputerCarts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCopyToClipboard implements IMessage {
    private String text;

    public MessageCopyToClipboard() {}

    public MessageCopyToClipboard(String text) {
        this.text = text;
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.text = ByteBufUtils.readUTF8String(input);
    }

    @Override
    public void toBytes(ByteBuf output) {
        ByteBufUtils.writeUTF8String(output, this.text);
    }

    public static final class Handler implements IMessageHandler<MessageCopyToClipboard, IMessage> {
        @Override
        public IMessage onMessage(MessageCopyToClipboard message, MessageContext context) {
            Minecraft client = Minecraft.getMinecraft();
            EntityPlayerSP player = client.player;
            if (player != null) {
                GuiScreen.setClipboardString(message.text);
                player.sendMessage(new TextComponentTranslation("chat." + ComputerCarts.MODID + ".clipboard"));
            }
            return null;
        }
    }
}
