package mods.computercarts.common.tank;

import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.network.Environment;
import mods.computercarts.common.minecart.EntityComputerCart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TankCart implements MultiTank {
    private List<IFluidTank> tanks = new ArrayList<>();
    private final EntityComputerCart cart;
    private int selectedTank;

    public TankCart(EntityComputerCart cart) {
        this.cart = cart;
    }

    @Override
    public int tankCount() {
        return tanks.size();
    }

    @Override
    public IFluidTank getFluidTank(int index) {
        if (checkIndex(index)) {
            return tanks.get(index);
        }
        return null;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound output) {
        output.setInteger("selected", selectedTank);
        return output;
    }

    public void readFromNBT(NBTTagCompound input) {
        if (input.hasKey("selected", 3)) {
            selectedTank = input.getInteger("selected");
        }
    }

    public int getSelectedTank() {
        return selectedTank;
    }

    public void setSelectedTank(int selectedTank) {
        this.selectedTank = selectedTank;
    }

    public void recalculateSize() {
        List<IFluidTank> tanks = new LinkedList<>();
        for (int i = 0; i < this.cart.componentInventory.getSizeInventory(); i++) {
            Environment env = this.cart.componentInventory.getSlotComponent(i);
            if (env instanceof IFluidTank) {
                tanks.add((IFluidTank) env);
            }
        }
        this.tanks.clear();
        this.tanks.addAll(tanks);
    }

    public boolean checkIndex(int index) {
        return index >= 0 && index < tanks.size();
    }
}
