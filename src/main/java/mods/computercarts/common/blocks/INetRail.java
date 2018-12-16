package mods.computercarts.common.blocks;

import li.cil.oc.api.network.Environment;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INetRail {
    Environment getResponseEnvironment(World world, BlockPos pos);

    boolean isValid(World world, BlockPos pos, EntityMinecart cart);
}
