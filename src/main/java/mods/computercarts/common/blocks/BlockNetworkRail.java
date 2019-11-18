package mods.computercarts.common.blocks;

import li.cil.oc.api.network.Environment;
import mods.computercarts.ComputerCarts;
import mods.computercarts.common.tileentity.TileEntityNetworkRailController;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockNetworkRail extends BlockRailBase implements NetRail {
    public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", EnumRailDirection.class, dir -> dir != EnumRailDirection.NORTH_EAST && dir != EnumRailDirection.NORTH_WEST && dir != EnumRailDirection.SOUTH_EAST && dir != EnumRailDirection.SOUTH_WEST);

    protected BlockNetworkRail() {
        super(false);
        this.setUnlocalizedName(ComputerCarts.MODID + ".network_rail");
        this.setRegistryName("network_rail");
        this.setHardness(0.7F);
    }

    @Override
    public boolean isFlexibleRail(IBlockAccess blockAccessor, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canMakeSlopes(IBlockAccess blockAccessor, BlockPos pos) {
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SHAPE, EnumRailDirection.byMetadata(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SHAPE).getMetadata();
    }

    @Override
    public IProperty<EnumRailDirection> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public Environment getResponseEnvironment(World world, BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos.down());
        if (entity instanceof TileEntityNetworkRailController)
            return ((TileEntityNetworkRailController) entity).getRailPlug();
        return null;
    }

    @Override
    public boolean isValid(World world, BlockPos pos, EntityMinecart cart) {
        return (cart instanceof Environment) && cart.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 0.5 && cart.world == world;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SHAPE);
    }
}
