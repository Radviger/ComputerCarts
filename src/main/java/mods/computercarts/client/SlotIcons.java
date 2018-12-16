package mods.computercarts.client;

import li.cil.oc.api.driver.item.Slot;
import mods.computercarts.Settings;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SlotIcons {

    private final static String[] SLOT_TYPES = new String[]{
            Slot.Card, Slot.ComponentBus, Slot.Container, Slot.CPU, "eeprom", Slot.Floppy, Slot.HDD, Slot.Memory,
            Slot.Tablet, "tool", Slot.Upgrade};


    private static Map<Integer, TextureAtlasSprite> tiericons = new HashMap<>();
    private static Map<String, TextureAtlasSprite> sloticons = new HashMap<>();

    public static void register(TextureMap map) {
        for (String slotType : SLOT_TYPES) {
            sloticons.put(slotType, map.registerSprite(new ResourceLocation(Settings.OC_ResLoc + ":icons/" + slotType)));
        }

        tiericons.put(-1, map.registerSprite(new ResourceLocation(Settings.OC_ResLoc + ":icons/na")));
        for (int i = 0; i < 3; i += 1) {
            tiericons.put(i, map.registerSprite(new ResourceLocation(Settings.OC_ResLoc + ":icons/tier" + i)));
        }
    }

    public static TextureAtlasSprite fromTier(int tier) {
        if (tiericons.containsKey(tier)) return tiericons.get(tier);
        return null;
    }

    public static TextureAtlasSprite fromSlot(String slot) {
        if (sloticons.containsKey(slot)) return sloticons.get(slot);
        return null;
    }
}
