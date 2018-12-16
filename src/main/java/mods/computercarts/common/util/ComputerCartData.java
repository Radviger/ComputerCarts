package mods.computercarts.common.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ComputerCartData {

    private Int2ObjectMap<ItemStack> components = new Int2ObjectOpenHashMap<>();
    private int tier = -1;
    private double energy = -1;
    //private String emblem = "";

    public void saveItemData(NBTTagCompound nbt) {
        if (nbt != null) {
            NBTTagList list = new NBTTagList();
            for (Int2ObjectMap.Entry<ItemStack> e : components.int2ObjectEntrySet()) {
                if (e.getValue() != null) {
                    NBTTagCompound slot = new NBTTagCompound();
                    slot.setTag("item", e.getValue().writeToNBT(new NBTTagCompound()));
                    slot.setInteger("slot", e.getIntKey());
                    list.appendTag(slot);
                }
            }

            nbt.setTag("componentinv", list);
            nbt.setInteger("tier", tier);
            nbt.setDouble("energy", energy);
            //nbt.setString("emblem", emblem);
        }
    }

    public void loadItemData(NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("componentinv")) {
                NBTTagList list = (NBTTagList) nbt.getTag("componentinv");
                for (int i = 0; i < list.tagCount(); i += 1) {
                    NBTTagCompound invslot = list.getCompoundTagAt(i);
                    components.put(invslot.getInteger("slot"), new ItemStack(invslot.getCompoundTag("item")));
                }
            }

            if (nbt.hasKey("energy")) this.energy = nbt.getDouble("energy");
            if (nbt.hasKey("tier")) this.tier = nbt.getInteger("tier");
            //if (nbt.hasKey("emblem")) this.emblem = nbt.getString("emblem");
        }
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Int2ObjectMap<ItemStack> getComponents() {
        return components;
    }

    public void setComponents(Int2ObjectMap<ItemStack> components) {
        this.components = components;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /*public String getEmblem() {
        return emblem;
    }

    public void setEmblem(String emblem) {
        this.emblem = emblem;
    }*/
}
