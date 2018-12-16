package mods.computercarts.common.entityextend;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import li.cil.oc.api.API;
import net.minecraft.entity.item.EntityMinecart;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoteExtenderRegister {

    private static List<RemoteCartExtender> updater = new ArrayList<>();

    public static boolean hasRemote(EntityMinecart cart) {
        return cart.hasCapability(RemoteCartExtender.CAPABILITY, null);
    }

    public static int enableRemote(EntityMinecart cart, boolean b) {
        if (!hasRemote(cart)) return 1;
        RemoteCartExtender ext = RemoteCartExtender.getFor(cart, cart.world);
        if (ext == null) return 1;
        if (ext.isEnabled() == b) return 2;
        ext.setEnabled(b);
        return 0;
    }

    public static boolean isRemoteEnabled(EntityMinecart cart) {
        if (!hasRemote(cart)) return false;
        RemoteCartExtender ext = RemoteCartExtender.getFor(cart, cart.world);
        if (ext == null) return false;
        return ext.isEnabled();
    }


    public static boolean addRemoteUpdate(RemoteCartExtender ext) {
        if (updater.contains(ext)) return false;
        updater.add(ext);
        return true;
    }

    public static boolean containsEntity(UUID uuid) {
        if (updater.isEmpty() || uuid == null) return false;
        for (RemoteCartExtender remoteCartExtender : updater) {
            if (remoteCartExtender.entity != null && remoteCartExtender.entity.getUniqueID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static void removeRemoteUpdate(EntityMinecart entity) {
        if (updater.isEmpty() || entity == null) return;
        for (RemoteCartExtender e : updater) {
            if (!e.isEnabled() || e.entity.getUniqueID() == entity.getUniqueID()) {
                e.setEnabled(false, true);
            }
        }
    }

    public static boolean removeRemoteUpdate(RemoteCartExtender ext) {
        if (!updater.contains(ext)) return false;
        updater.remove(ext);
        API.network.leaveWirelessNetwork(ext); //Sometimes it doesn't leave the network (Dimension changing)
        return true;
    }

    public static void serverTick() {
        IntList marked = new IntArrayList();
        for (int i = 0; i < updater.size(); i += 1) {
            if (updater.get(i).isEnabled())
                updater.get(i).update();
            else
                marked.add(i);
        }

        if (!marked.isEmpty()) {
            for (int i : marked) {
                updater.remove(i);
            }
        }
    }

    public static RemoteCartExtender getExtender(EntityMinecart cart) {
        if (!hasRemote(cart)) return null;
        return RemoteCartExtender.getFor(cart, cart.world);
    }

    public static void reinit() {
        updater.clear();
    }

    public static void register() {
        /*if (Loader.isModLoaded("Railcraft")) {
            registerRemote(mods.railcraft.common.carts.EntityLocomotiveSteamSolid.class, RemoteSteamLocomotive.class);
            registerRemote(mods.railcraft.common.carts.EntityLocomotiveElectric.class, RemoteElectricLocomotive.class);
        }*/
    }

}
