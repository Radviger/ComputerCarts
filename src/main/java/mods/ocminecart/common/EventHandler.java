package mods.ocminecart.common;

import mods.ocminecart.client.SlotIcons;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class EventHandler {
	
	public static void initHandler(){
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		//FMLCommonHandler.instance().bus().register(handler);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemIconRegister(TextureStitchEvent event) {
		if(event.map.getTextureType()==1) SlotIcons.register(event.map);
	}
}