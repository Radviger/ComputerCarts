package mods.computercarts.common.items;

import mods.computercarts.ComputerCarts;
import mods.computercarts.Settings;
import mods.computercarts.common.assemble.util.TooltipUtil;
import mods.computercarts.common.entityextend.RemoteCartExtender;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import mods.computercarts.common.items.interfaces.ItemEntityInteract;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.ItemUseMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemCartRemoteModule extends Item implements ItemEntityInteract {

    public static int[] range;

    public ItemCartRemoteModule() {
        super();
        this.setMaxStackSize(64);
        this.setUnlocalizedName(ComputerCarts.MODID + ".remote_module");
        this.setRegistryName("remote_module");
        this.setHasSubtypes(true);
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) range = Settings.RemoteRange;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack item, IBlockAccess blockAccessor, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack item) {
        return this.getDisplayString(item, false);
    }

    @Override
    public boolean onEntityClick(EntityPlayer p, Entity e, ItemStack s, Type t, EnumHand h) {
        if ((e instanceof EntityMinecart) && t == Type.RIGHT_CLICK) {
            if (p.world.isRemote) return true;
            int err = RemoteExtenderRegister.enableRemote((EntityMinecart) e, true);
            if (err == 0) {
                RemoteCartExtender ext = RemoteExtenderRegister.getExtender((EntityMinecart) e);
                if (ext != null) {
                    ext.setRemoteItem(s);
                    ext.setMaxWlanStrength(getRangeByTier(s.getItemDamage()));
                    ext.setOwner(p.getUniqueID().toString());
                }
            }

            NBTTagCompound data = new NBTTagCompound();
            data.setDouble("posX", e.posX);
            data.setDouble("posY", e.posY);
            data.setDouble("posZ", e.posZ);
            data.setByte("error", (byte) err);
            ModNetwork.sendToNearPlayers(new ItemUseMessage(0, p.getEntityId(), data, h), e.posX, e.posY, e.posZ, e.world);
            if (!p.capabilities.isCreativeMode && err == 0) s.shrink(1);

            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void onMPUsage(EntityPlayer p, NBTTagCompound data, EnumHand hand) {
        World world = p.world;
        double posX = data.getDouble("posX");
        double posY = data.getDouble("posY");
        double posZ = data.getDouble("posZ");
        byte error = data.getByte("error");
        Random r = new Random();
        if (error == 0) {
            p.swingArm(hand);
            if (p.equals(Minecraft.getMinecraft().player))
                p.sendMessage(new TextComponentString(TextFormatting.GREEN + I18n.translateToLocal("chat." + ComputerCarts.MODID + ".moduleinstalled")));
        } else if (error == 1)
            p.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("chat." + ComputerCarts.MODID + ".invalidcart")));
        else
            p.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("chat." + ComputerCarts.MODID + ".hasmodule")));
        for (int i = 0; i < 100; i++) {
            if (error == 0)
                world.spawnParticle(EnumParticleTypes.TOWN_AURA, posX + (r.nextDouble() - 0.5) * 1.4, posY + (r.nextDouble() - 0.5) * 1.4, posZ + (r.nextDouble() - 0.5) * 1.4, 0, 0, 0);
            else
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX + (r.nextDouble() - 0.5) * 1.4, posY - 0.3, posZ + (r.nextDouble() - 0.5) * 1.4, 0, 0, 0);
        }
    }

    public int getRangeByTier(int tier) {
        switch (tier) {
            case 1:
                return range[1];
            case 2:
                return range[2];
            default:
                return range[0];
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < 3; i += 1) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    public String getDisplayString(ItemStack stack, boolean hasColor) {
        TextFormatting color;
        String tier;
        tier = I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".tier" + (stack.getItemDamage() + 1));
        switch (stack.getItemDamage()) {
            case 0:
                color = TextFormatting.WHITE;
                break;
            case 1:
                color = TextFormatting.YELLOW;
                break;
            case 2:
                color = TextFormatting.AQUA;
                break;
            default:
                color = TextFormatting.DARK_RED;
                tier = "ERROR!";
                break;
        }
        if (!hasColor) {
            color = TextFormatting.RESET;
        }
        return color + super.getItemStackDisplayName(stack) + " " + tier;
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> lines, ITooltipFlag adv) {
        lines.clear();
        lines.add(this.getDisplayString(item, true));

        if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
            String key = GameSettings.getKeyDisplayString(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
            String formkey = "[" + TextFormatting.WHITE + key + TextFormatting.GRAY + "]";
            lines.add(I18n.translateToLocalFormatted("tooltip." + ComputerCarts.MODID + ".moreinfo", formkey));
        } else {
            lines.addAll(TooltipUtil.trimString(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".remote_module.desc")));
            lines.add("Max. Range: " + TextFormatting.WHITE + getRangeByTier(item.getItemDamage()));
            lines.add("Railcraft is " + (!Loader.isModLoaded("Railcraft") ? TextFormatting.RED + "not " : TextFormatting.GREEN) + "avaiable");
        }
    }

}
