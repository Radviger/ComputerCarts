package mods.computercarts.common;

import mods.computercarts.client.gui.GuiComputerCart;
import mods.computercarts.client.gui.GuiNetworkRailBase;
import mods.computercarts.client.gui.GuiRemoteModule;
import mods.computercarts.common.container.ComputerCartContainer;
import mods.computercarts.common.container.NetworkRailBaseContainer;
import mods.computercarts.common.container.RemoteModuleContainer;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (y != -10) {            //if y is -10 then we send the Entity ID with the par. x
            TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity != null) {
                switch (ID) {
                    case 0:
                        if (entity instanceof TileEntityNetworkRailBase)
                            return new NetworkRailBaseContainer(player.inventory, (TileEntityNetworkRailBase) entity);
                }
            }
        } else {
            Entity entity = world.getEntityByID(x);
            if (entity != null) {
                switch (ID) {
                    case 1:
                        if (entity instanceof EntityComputerCart)
                            return new ComputerCartContainer(player.inventory, (EntityComputerCart) entity);
                        break;
                    case 2:
                        if ((entity instanceof EntityMinecart) && RemoteExtenderRegister.isRemoteEnabled((EntityMinecart) entity)) {
                            return new RemoteModuleContainer((EntityMinecart) entity);
                        }
                        break;
                }
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (y != -10) {            //if y is -10 then we send the Entity ID with the par. x
            TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity != null) {
                switch (ID) {
                    case 0:
                        if (entity instanceof TileEntityNetworkRailBase)
                            return new GuiNetworkRailBase(player.inventory, (TileEntityNetworkRailBase) entity);
                }
            }
        } else {
            Entity entity = world.getEntityByID(x);
            if (entity != null) {
                switch (ID) {
                    case 1:
                        if (entity instanceof EntityComputerCart)
                            return new GuiComputerCart(player.inventory, (EntityComputerCart) entity);
                        break;
                    case 2:
                        if (entity instanceof EntityMinecart) {
                            return new GuiRemoteModule();
                        }
                }
            }
        }

        return null;
    }

}
