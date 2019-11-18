package mods.computercarts.network.message;

import io.netty.buffer.ByteBuf;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageNetworkMode implements IMessage {
    private BlockPos pos;
    private byte mode;

    public MessageNetworkMode() {}

    public MessageNetworkMode(BlockPos pos, byte mode) {
        this.pos = pos;
        this.mode = mode;
    }

    @Override
    public void fromBytes(ByteBuf input) {
        this.pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
        this.mode = input.readByte();
    }

    @Override
    public void toBytes(ByteBuf output) {
        output.writeInt(this.pos.getX());
        output.writeInt(this.pos.getY());
        output.writeInt(this.pos.getZ());
        output.writeByte(this.mode);
    }

    public static final class Handler implements IMessageHandler<MessageNetworkMode, IMessage> {

        @Override
        public IMessage onMessage(MessageNetworkMode message, MessageContext context) {
            NetHandlerPlayServer netHandler = context.getServerHandler();
            EntityPlayerMP sender = netHandler.player;
            World world = sender.getEntityWorld();

            if (world.isBlockLoaded(message.pos)) {
                TileEntity tile = world.getTileEntity(message.pos);
                if (tile instanceof TileEntityNetworkRailController) {
                    TileEntityNetworkRailController controller = (TileEntityNetworkRailController) tile;
                    controller.onButtonPress(message.mode);
                }
            }
            return null;
        }
    }
}
