package mods.computercarts.common.container.slots;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Container;
import li.cil.oc.common.Tier;
import mods.computercarts.client.SlotIcons;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Objects;

public class SlotComponent extends Slot {

    private int tier;
    private String type;

    public SlotComponent(IInventory inventory, int index, int xpos, int ypos, int tier, String type) {
        super(inventory, index, xpos, ypos);
        this.tier = tier;
        this.type = type;
    }

    public SlotComponent(IInventory inventory, int index, int xpos, int ypos, ItemStack container) {
        super(inventory, index, xpos, ypos);

        DriverItem driver = Driver.driverFor(container);
        if (driver instanceof Container) {
            this.tier = ((Container) driver).providedTier(container);
            this.type = ((Container) driver).providedSlot(container);
        } else {
            this.tier = Tier.None();
            this.type = li.cil.oc.api.driver.item.Slot.None;
        }
    }

    @Nullable
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return SlotIcons.fromTier(this.tier);
    }

    public String getSlotType() {
        return this.type;
    }

    public boolean isItemValid(ItemStack stack) {
        if (Objects.equals(this.type, li.cil.oc.api.driver.item.Slot.None) || this.tier == Tier.None()) return false;
        else if (Objects.equals(this.type, li.cil.oc.api.driver.item.Slot.Any) && this.tier == Tier.Any()) return true;

        DriverItem drv = Driver.driverFor(stack);
        if (drv == null) return false;
        if ((Objects.equals(drv.slot(stack), this.type) || Objects.equals(drv.slot(stack), li.cil.oc.api.driver.item.Slot.Any)) && drv.tier(stack) <= this.tier)
            return this.inventory.isItemValidForSlot(this.slotNumber, stack);

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isEnabled() {
        return !Objects.equals(type, li.cil.oc.api.driver.item.Slot.None) && tier != Tier.None();
    }

    public int getTier() {
        return this.tier;
    }

}