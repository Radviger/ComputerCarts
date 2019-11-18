package mods.computercarts.common.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mods.computercarts.ComputerCarts;
import mods.computercarts.common.items.interfaces.ComponentInventoryItem;
import mods.computercarts.common.minecart.EntityComputerCart;
import mods.computercarts.common.util.ComputerCartData;
import mods.computercarts.common.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemComputerCart extends ItemAdvancedMinecart implements ComponentInventoryItem {
    public ItemComputerCart() {
        this.setUnlocalizedName(ComputerCarts.MODID + ".computer_cart");
        this.setRegistryName("computer_cart");
        this.setHasSubtypes(true);
    }

    @Override
    public EntityMinecart create(World world, double x, double y, double z, ItemStack stack) {
        ComputerCartData data = ItemUtil.getCartData(stack);
        return data != null ? new EntityComputerCart(world, x, y, z, data) : null;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            ItemStack cart = new ItemStack(ModItems.COMPUTER_CART, 1);
            ComputerCartData data = new ComputerCartData();
            Int2ObjectMap<ItemStack> components = new Int2ObjectOpenHashMap<>();
            //Container
            components.put(0, ModItems.getOCItem("cardcontainer3"));
            components.put(1, ModItems.getOCItem("upgradecontainer3"));
            components.put(2, ModItems.getOCItem("diskdrivemountable"));
            //Upgrades
            components.put(3, ModItems.getOCItem("inventoryupgrade"));
            components.put(4, ModItems.getOCItem("inventoryupgrade"));
            components.put(5, ModItems.getOCItem("inventoryupgrade"));
            components.put(6, ModItems.getOCItem("inventoryupgrade"));
            components.put(7, ModItems.getOCItem("inventorycontrollerupgrade"));
            components.put(8, ModItems.getOCItem("tankupgrade"));
            components.put(9, ModItems.getOCItem("tankcontrollerupgrade"));
            components.put(10, ModItems.getOCItem("screen1"));
            components.put(11, ModItems.getOCItem("keyboard"));
            //Cards
            components.put(12, ModItems.getOCItem("internetcard"));
            components.put(13, ModItems.getOCItem("wlancard2"));
            components.put(14, ItemStack.EMPTY);
            //CPU - APU
            components.put(15, ModItems.getOCItem("apucreative"));
            //RAM
            components.put(16, ModItems.getOCItem("ram6"));
            components.put(17, ModItems.getOCItem("ram6"));
            //EEPROM
            components.put(18, ModItems.getOCItem("luabios"));
            //HDD
            components.put(19, ModItems.getOCItem("hdd3"));
            data.setComponents(components);
            data.setTier(3);
            data.setEnergy(20000);
            ItemUtil.setCartData(cart, data);
            items.add(cart);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && ItemUtil.getCartData(player.getHeldItem(hand)) == null) {
            player.setHeldItem(hand, ItemStack.EMPTY);
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Sorry! Removed the invalid item"));
            return EnumActionResult.FAIL;
        } else {
            return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
        }
    }

    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack item) {
        return this.getDisplayString(item, false);
    }

    private String getDisplayString(@Nonnull ItemStack stack, boolean colored) {
        TextFormatting color;
        ComputerCartData data = ItemUtil.getCartData(stack);
        String tier = data == null ? "ERROR!" : I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".tier" + (data.getTier() + 1));
        switch (data != null ? data.getTier() : -1) {
            case 0:
                color = TextFormatting.WHITE;
                break;
            case 1:
                color = TextFormatting.YELLOW;
                break;
            case 2:
                color = TextFormatting.AQUA;
                break;
            case 3:
                color = TextFormatting.LIGHT_PURPLE;
                break;
            default:
                color = TextFormatting.DARK_RED;
                break;
        }
        if (!colored) color = TextFormatting.RESET;
        return color + super.getItemStackDisplayName(stack) + " " + tier;
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> lines, ITooltipFlag adv) {
        super.addInformation(item, world, lines, adv);
        lines.clear();
        lines.add(this.getDisplayString(item, true));
        ComputerCartData data = ItemUtil.getCartData(item);
        if (data == null) return;
        String eloc = I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".storedenergy");
        lines.add(TextFormatting.WHITE + eloc + ": " + TextFormatting.GREEN + String.format("%.0f", data.getEnergy()));
        /*String emblemid = data.getEmblem();
          if (emblemid != null && emblemid != "" && Loader.isModLoaded("Railcraft")) {
              Emblem emblem = EmblemToolsClient.packageManager.getEmblem(emblemid);
              if (emblem != null) {
                  lines.add(TextFormatting.GOLD + StatCollector.translateToLocal("tooltip." + ComputerCarts.MODID + ".emblem") +
                          TextFormatting.WHITE + " " + emblem.displayName);
              }
          }*/
        if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
            String key = GameSettings.getKeyDisplayString(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
            String formkey = "[" + TextFormatting.WHITE + key + TextFormatting.GRAY + "]";
            lines.add(I18n.translateToLocalFormatted("tooltip." + ComputerCarts.MODID + ".viewcomponents", formkey));
        } else {
            lines.add(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".instcomponents") + ":");
            for (ItemStack i : data.getComponents().values()) {
                lines.add("- " + i.getDisplayName());
            }
        }
    }

    @Override
    public void setComponents(@Nonnull ItemStack stack, Int2ObjectMap<ItemStack> components) {
        ComputerCartData data = ItemUtil.getCartData(stack);
        if (data != null) {
            data.setComponents(components);
            ItemUtil.setCartData(stack, data);
        }
    }

    @Override
    public Int2ObjectMap<ItemStack> getComponents(@Nonnull ItemStack stack) {
        ComputerCartData data = ItemUtil.getCartData(stack);
        return data != null ? data.getComponents() : null;
    }
}
