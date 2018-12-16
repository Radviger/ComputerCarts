package mods.computercarts.common.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class RotationHelper {
    public static EnumFacing[] dir = {
        EnumFacing.SOUTH,
        EnumFacing.EAST,
        EnumFacing.NORTH,
        EnumFacing.WEST
    };

    public static EnumFacing calcLocalDirection(EnumFacing value, EnumFacing face) {
        int n = indexHelperArray(face);
        int d = indexHelperArray(value);
        if (n < 0 || d < 0) return value;
        return dir[(d + n + 4) % 4];
    }

    public static EnumFacing calcGlobalDirection(EnumFacing value, EnumFacing face) {
        int n = indexHelperArray(face);
        int d = indexHelperArray(value);
        if (n < 0 || d < 0) return value;
        return dir[(d - n + 4) % 4];
    }

    public static int indexHelperArray(EnumFacing direction) {
        for (int i = 0; i < dir.length; i += 1) {
            if (dir[i] == direction) return i;
        }
        return -1;
    }

    //http://jabelarminecraft.blogspot.co.at/p/minecraft-forge-172-finding-block.html
    public static EnumFacing directionFromYaw(double yaw) {
        yaw += 44.5;
        yaw = (yaw + 360) % 360;
        int di = MathHelper.floor((yaw * 4.0D / 360D) + 0.5D);
        di = (di + 4) % 4;
        return RotationHelper.dir[di];
    }

    public static double calcAngle(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dy = z1 - z2;
        return (Math.atan2(dy, dx) * 180D) / Math.PI;
    }
}
