package mods.computercarts.network;

import mods.computercarts.ComputerCarts;
import mods.computercarts.network.message.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork {

    public static SimpleNetworkWrapper CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ComputerCarts.MODID.toLowerCase());
        int id = -1;

        CHANNEL.registerMessage(MessageRemoteModuleSetup.Handler.class, MessageRemoteModuleSetup.class, id++, Side.SERVER);
        CHANNEL.registerMessage(MessagePowerButton.Handler.class, MessagePowerButton.class, id++, Side.SERVER);
        CHANNEL.registerMessage(MessageNetworkMode.Handler.class, MessageNetworkMode.class, id++, Side.SERVER);
        CHANNEL.registerMessage(MessageEntitySyncRequest.Handler.class, MessageEntitySyncRequest.class, id++, Side.SERVER);
        CHANNEL.registerMessage(MessageEntitySyncResponse.Handler.class, MessageEntitySyncResponse.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(MessageCopyToClipboard.Handler.class, MessageCopyToClipboard.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(MessageConfigSync.Handler.class, MessageConfigSync.class, id++, Side.CLIENT);
    }

    public static void sendColoredMessage(EntityPlayer target, TextFormatting color, ITextComponent message) {
        message.getStyle().setColor(color);
        target.sendMessage(message);
    }

    public static void sendToNearPlayers(IMessage msg, TileEntity entity) {
        BlockPos pos = entity.getPos();
        sendToNearPlayers(msg, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entity.getWorld());
    }

    public static void sendToNearPlayers(IMessage msg, double x, double y, double z, World world) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        for (EntityPlayerMP player : playerList.getPlayers()) {
            int serverview = playerList.getViewDistance() * 16;
            if (player.getDistance(x, y, z) <= serverview && world.provider.getDimension() == player.dimension) {
                CHANNEL.sendTo(msg, player);
            }
        }
    }
}
