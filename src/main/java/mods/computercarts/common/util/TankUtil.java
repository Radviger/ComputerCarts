package mods.computercarts.common.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TankUtil {

    public static IFluidHandler getFluidHandler(World w, BlockPos pos, EnumFacing side) {
        if (w.isRemote) return null;
        TileEntity entity = w.getTileEntity(pos);
        return entity != null ? entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side) : null;
    }

    public static int getSpaceForFluid(IFluidHandler tank, FluidStack stack) {
        FluidStack fs = stack.copy();
        fs.amount = Integer.MAX_VALUE;
        return tank.fill(fs, false);
    }

    public static boolean hasFluid(IFluidHandler tank, FluidStack stack) {
        return tank.drain(stack, false) != null;
    }
}
