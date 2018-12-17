package mods.computercarts.common.container;

import mods.computercarts.common.entityextend.RemoteCartExtender;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class RemoteModuleContainer extends Container {

    protected final boolean IS_SERVER = FMLCommonHandler.instance().getEffectiveSide().isServer();
    private RemoteCartExtender module;
    private EntityMinecart cart;

    private List<EntityPlayer> toBan;

    @SideOnly(Side.CLIENT)
    public int passstate;
    @SideOnly(Side.CLIENT)
    public boolean perm;

    public boolean locked = false;

    public RemoteModuleContainer() {
        super();
    }

    public RemoteModuleContainer(EntityMinecart cart) {
        super();

        if (IS_SERVER) toBan = new ArrayList<>();
        else {
            passstate = 0;
            perm = false;
        }

        this.cart = cart;
        this.module = RemoteExtenderRegister.getExtender(cart);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        if (IS_SERVER) {
            if (this.toBan.size() > 0 && this.toBan.contains(player)) return false;
            return player.world == cart.world && player.getDistanceSq(cart.posX, cart.posY, cart.posZ) <= 64 && module.isEnabled();
        }
        return true;
    }

    public RemoteCartExtender getModule() {
        return module;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.module.isLocked() != this.locked) {
                listener.sendWindowProperty(this, 2, (module.isLocked()) ? 1 : 0);
            }
        }

        this.locked = this.module.isLocked();
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if (!IS_SERVER) return;
        listener.sendWindowProperty(this, 0, 0);
        if (listener instanceof EntityPlayer)
            listener.sendWindowProperty(this, 1, module.editableByPlayer((EntityPlayer) listener, true) ? 1 : 0);
        else
            listener.sendWindowProperty(this, 1, 0);
        listener.sendWindowProperty(this, 2, (module.isLocked()) ? 1 : 0);
    }

    public void sendPassState(EntityPlayer p, int i) {
        if (p instanceof IContainerListener) {
            ((IContainerListener) p).sendWindowProperty(this, 0, i);
        }
    }

    public void lockGui() {
        for (IContainerListener listener : this.listeners) {
            if (!(listener instanceof EntityPlayer)) continue;
            if (!module.editableByPlayer((EntityPlayer) listener, false)) {
                this.toBan.add((EntityPlayer) listener);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int field, int value) {
        switch (field) {
            case 0:
                this.passstate = value;
                break;
            case 1:
                this.perm = (value == 1);
                break;
            case 2:
                this.locked = (value == 1);
                break;
        }
    }
}
