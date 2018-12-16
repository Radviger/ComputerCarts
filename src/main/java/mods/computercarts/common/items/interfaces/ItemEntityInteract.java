package mods.computercarts.common.items.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public interface ItemEntityInteract {

    boolean onEntityClick(EntityPlayer p, Entity e, ItemStack s, Type t, EnumHand h);

    enum Type {
        LEFT_CLICK,
        RIGHT_CLICK
    }
}
