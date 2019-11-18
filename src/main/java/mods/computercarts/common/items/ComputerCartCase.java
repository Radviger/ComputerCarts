package mods.computercarts.common.items;

import mods.computercarts.ComputerCarts;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ComputerCartCase extends Item {
    ComputerCartCase() {
        super();
        this.setHasSubtypes(true);
        this.setTranslationKey(ComputerCarts.MODID + ".computer_cart_case");
        this.setRegistryName("computer_cart_case");
        this.setMaxStackSize(1);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < 4; i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> lines, ITooltipFlag adv) {
        TextFormatting color = TextFormatting.RESET;
        switch (item.getItemDamage()) {
            case 0:  //Tier 1
                color = TextFormatting.WHITE;
                break;
            case 1:  //Tier 2
                color = TextFormatting.YELLOW;
                break;
            case 2:  //Tier 3
                color = TextFormatting.AQUA;
                break;
            case 3:  //Creative
                color = TextFormatting.LIGHT_PURPLE;
        }
        lines.clear();
        lines.add(color + this.getItemStackDisplayName(item) + " " + I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".tier" + (item.getItemDamage() + 1)));
    }
}
