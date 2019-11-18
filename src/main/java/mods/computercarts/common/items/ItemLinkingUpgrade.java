package mods.computercarts.common.items;

import mods.computercarts.ComputerCarts;
import mods.computercarts.common.assemble.util.TooltipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLinkingUpgrade extends Item {

    public ItemLinkingUpgrade() {
        super();
        this.setMaxStackSize(64);
        this.setTranslationKey(ComputerCarts.MODID + ".linking_upgrade");
        this.setRegistryName("linking_upgrade");
        this.setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack item, @Nullable World world, List<String> lines, ITooltipFlag adv) {
        if (!Loader.isModLoaded("Railcraft")) {
            lines.add(TextFormatting.RED + "RAILCRAFT IS NOT INSTALLED. ITEM IS USELESS");
        }

        if (!Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode())) {
            String key = GameSettings.getKeyDisplayString(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
            String formkey = "[" + TextFormatting.WHITE + key + TextFormatting.GRAY + "]";
            lines.add(I18n.translateToLocalFormatted("tooltip." + ComputerCarts.MODID + ".moreinfo", formkey));
        } else {
            lines.addAll(TooltipUtil.trimString(I18n.translateToLocal("tooltip." + ComputerCarts.MODID + ".linking_upgrade.desc")));
        }
    }
}
