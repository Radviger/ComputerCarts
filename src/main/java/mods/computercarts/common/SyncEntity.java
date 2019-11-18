package mods.computercarts.common;

import net.minecraft.nbt.NBTTagCompound;

public interface SyncEntity {
    NBTTagCompound writeSyncData(NBTTagCompound output);

    void readSyncData(NBTTagCompound input);
}
