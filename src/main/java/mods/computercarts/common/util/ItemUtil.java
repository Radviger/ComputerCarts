package mods.computercarts.common.util;

import mods.computercarts.common.items.ItemComputerCart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemUtil {
    public static void dropItems(Iterable<ItemStack> items, World world, double x, double y, double z, boolean motion) {
        if (!world.isRemote) {
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    EntityItem lyingItem = new EntityItem(world, x, y, z, item);
                    lyingItem.setDefaultPickupDelay();
                    if (!motion) {
                        lyingItem.motionX = 0;
                        lyingItem.motionY = 0;
                        lyingItem.motionZ = 0;
                    }
                    world.spawnEntity(lyingItem);
                }
            }
        }
    }

    public static void dropItem(@Nonnull ItemStack item, World world, double x, double y, double z, boolean motion) {
        if (!item.isEmpty()) {
            EntityItem lyingItem = new EntityItem(world, x, y, z, item);
            lyingItem.setDefaultPickupDelay();
            if (!motion) {
                lyingItem.motionX = 0;
                lyingItem.motionY = 0;
                lyingItem.motionZ = 0;
            }
            world.spawnEntity(lyingItem);
        }
    }

    @Nonnull
    public static ItemStack suckItems(World world, BlockPos pos, @Nonnull ItemStack filter, int num) {
        if (!world.isRemote) {
            AxisAlignedBB box = new AxisAlignedBB(1, 1, 1, 16, 16, 16);
            box.offset(pos.getZ() - 1, pos.getY() - 1, pos.getZ() - 1);
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, box);
            for (EntityItem item : items) {
                ItemStack stack = item.getItem();
                if (filter.isEmpty() || filter.isItemEqual(stack)) {
                    ItemStack ret = stack.copy();
                    ret.setCount(Math.min(ret.getCount(), num));
                    stack.shrink(ret.getCount());
                    if (stack.isEmpty()) item.setDead();
                    return ret;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasDroppedItems(World world, BlockPos pos) {
        if (!world.isRemote) {
            AxisAlignedBB box = new AxisAlignedBB(1, 1, 1, 16, 16, 16);
            box.offset(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1);
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, box);
            return !items.isEmpty();
        }
        return false;
    }

    @Nonnull
    public static ItemStack sumItemStacks(@Nonnull ItemStack stackA, @Nonnull ItemStack stackB, boolean pull) {
        ItemStack res;
        if (stackA.isEmpty() || stackB.isEmpty()) return ItemStack.EMPTY;
        if (!stackA.isItemEqual(stackB)) return ItemStack.EMPTY;
        res = stackA.copy();
        int size = stackA.getCount() + stackB.getCount();
        size = Math.min(size, res.getMaxStackSize());
        res.setCount(size);
        if (pull) {
            if (size >= stackA.getCount()) {
                size -= stackA.getCount();
                stackA.setCount(0);
            } else {
                stackA.shrink(size);
                size = 0;
            }

            if (size >= stackB.getCount()) {
                size -= stackB.getCount();
                stackB.setCount(0);
            } else {
                stackB.shrink(size);
                size = 0;
            }
        }
        return res;
    }

    public static ComputerCartData getCartData(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemComputerCart && stack.hasTagCompound()) {
            ComputerCartData data = new ComputerCartData();
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey("cartdata")) {
                data.loadItemData(nbt.getCompoundTag("cartdata"));
            }
            return data;
        }
        return null;
    }

    public static void setCartData(@Nonnull ItemStack stack, ComputerCartData data) {
        if (stack.getItem() instanceof ItemComputerCart) {
            NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
            if (data != null) {
                NBTTagCompound tag = new NBTTagCompound();
                data.saveItemData(tag);
                nbt.setTag("cartdata", tag);
            }
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(nbt);
            }
        }
    }
}
