package mods.computercarts.common.blocks;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.tileentity.TileEntityNetworkRailBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NetworkRailBase extends BlockContainer {

    protected NetworkRailBase() {
        super(Material.IRON);
        this.setUnlocalizedName(ComputerCarts.MODID + ".network_rail_base");
        this.setRegistryName("network_rail_base");
        this.setHardness(2F);
        this.setResistance(5f);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(ComputerCarts.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess blockaccessor, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EntityLiving.SpawnPlacementType spawnType) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess blockAccessor, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityNetworkRailBase();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityNetworkRailBase();
    }
}
