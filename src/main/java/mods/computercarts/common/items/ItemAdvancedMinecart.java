package mods.computercarts.common.items;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAdvancedMinecart extends Item {

    protected static final IBehaviorDispenseItem MINECART_DISPENSE_BHAVIOR = new BehaviorDefaultDispenseItem() {
        private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack item) {
            EnumFacing side = source.getBlockState().getValue(BlockDispenser.FACING);
            World world = source.getWorld();
            double d0 = source.getX() + (double) ((float) side.getFrontOffsetX() * 1.125F);
            double d1 = source.getY() + (double) ((float) side.getFrontOffsetY() * 1.125F);
            double d2 = source.getZ() + (double) ((float) side.getFrontOffsetZ() * 1.125F);
            BlockPos pos = source.getBlockPos().offset(side);
            IBlockState state = world.getBlockState(pos);
            double offset;

            if (BlockRailBase.isRailBlock(state)) {
                offset = 0;
            } else {
                if (state.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(pos.down()))) {
                    return this.behaviourDefaultDispenseItem.dispense(source, item);
                }

                offset = -1;
            }

            EntityMinecart cart = ((ItemAdvancedMinecart) item.getItem()).create(world, d0, d1 + offset, d2, item);

            if (item.hasDisplayName()) {
                cart.setCustomNameTag(item.getDisplayName());
            }

            world.spawnEntity(cart);
            item.splitStack(1);
            return item;
        }

        @Override
        protected void playDispenseSound(IBlockSource source) {
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
        }
    };

    protected ItemAdvancedMinecart() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSE_BHAVIOR);
        this.maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (BlockRailBase.isRailBlock(world.getBlockState(pos))) {
            ItemStack item = player.getHeldItem(hand);
            if (!world.isRemote) {
                EntityMinecart cart = this.create(world, (float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F, item);

                if (item.hasDisplayName()) {
                    cart.setCustomNameTag(item.getDisplayName());
                }

                world.spawnEntity(cart);
            }

            item.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public EntityMinecart create(World world, double x, double y, double z, ItemStack stack) {
        return EntityMinecart.create(world, x, y, z, EntityMinecart.Type.RIDEABLE);
    }
}
