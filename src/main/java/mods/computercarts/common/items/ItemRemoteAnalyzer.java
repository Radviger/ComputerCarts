package mods.computercarts.common.items;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.assemble.util.TooltipUtil;
import mods.computercarts.common.entityextend.RemoteCartExtender;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import mods.computercarts.common.items.interfaces.ItemEntityInteract;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.ItemUseMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRemoteAnalyzer extends Item implements ItemEntityInteract {

    public ItemRemoteAnalyzer() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(ComputerCarts.MODID + ".remote_analyzer");
        this.setRegistryName("remote_analyzer");
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack item, IBlockAccess blockAccessor, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean onEntityClick(EntityPlayer p, Entity e, ItemStack s, Type t, EnumHand h) {
        if (e instanceof EntityMinecart) {
            if (p.world.isRemote) return true;
            RemoteCartExtender extender = RemoteExtenderRegister.getExtender((EntityMinecart) e);
            if (extender != null && extender.isEnabled()) {
                if (t == Type.RIGHT_CLICK) {
                    extender.onAnalyzeModule(p);

                    NBTTagCompound usedat = new NBTTagCompound();
                    usedat.setString("address", extender.getAddress());
                    ModNetwork.sendToNearPlayers(new ItemUseMessage(1, p.getEntityId(), usedat, h), e.posX, e.posY, e.posZ, e.world);
                } else if (t == Type.LEFT_CLICK) {
                    if (extender.editableByPlayer(p, false)) {
                        p.openGui(ComputerCarts.INSTANCE, 2, e.world, e.getEntityId(), -10, 0);
                    } else {
                        NBTTagCompound usedat = new NBTTagCompound();
                        usedat.setInteger("type", 1);
                        ModNetwork.CHANNEL.sendTo(new ItemUseMessage(1, p.getEntityId(), usedat, h), (EntityPlayerMP) p);
                    }
                }
            } else if (RemoteExtenderRegister.hasRemote((EntityMinecart) e)) {
                p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "No Module found."));
            } else {
                p.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "No Module installable."));
            }
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void onMPUsage(EntityPlayer p, NBTTagCompound data, EnumHand hand) {
        if (p != Minecraft.getMinecraft().player) return;
        if (data.hasKey("type") && data.getInteger("type") == 1) {
            p.sendMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("chat." + ComputerCarts.MODID + ".owneronly")));
        } else if (p.isSneaking() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            GuiScreen.setClipboardString(data.getString("address"));
            p.sendMessage(new TextComponentTranslation("chat." + ComputerCarts.MODID + ".clipboard"));
        }
        p.swingArm(hand);
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> lines, ITooltipFlag adv) {
        if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
            String key = GameSettings.getKeyDisplayString(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
            String formkey = "[" + TextFormatting.WHITE + key + TextFormatting.GRAY + "]";
            lines.add(I18n.translateToLocalFormatted("tooltip." + ComputerCarts.MODID + ".moreinfo", formkey));
        } else {
            lines.addAll(TooltipUtil.trimString(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".remoteanalyzer.desc")));
        }
    }
}
