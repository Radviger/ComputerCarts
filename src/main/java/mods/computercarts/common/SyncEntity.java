package mods.computercarts.common;

import net.minecraft.nbt.NBTTagCompound;

public interface SyncEntity {
    void writeSyncData(NBTTagCompound nbt);

    void readSyncData(NBTTagCompound nbt);
}
