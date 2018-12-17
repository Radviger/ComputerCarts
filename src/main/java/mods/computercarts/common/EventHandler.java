package mods.computercarts.common;

import mods.computercarts.Settings;
import mods.computercarts.client.SlotIcons;
import mods.computercarts.common.blocks.ModBlocks;
import mods.computercarts.common.entityextend.RemoteExtenderRegister;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.items.interfaces.ItemEntityInteract;
import mods.computercarts.common.recipe.event.CraftingHandler;
import mods.computercarts.network.ModNetwork;
import mods.computercarts.network.message.ConfigSyncMessage;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mods.computercarts.ComputerCarts.MODID;
import static net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation;


public class EventHandler {

    int ticks = 0; // 40 Server Ticks/sec but we want only 20

    public static void initHandler() {
        EventHandler handler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemIconRegister(TextureStitchEvent.Pre event) {
        SlotIcons.register(event.getMap());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemModelRegister(ModelRegistryEvent event) {
        setCustomModelResourceLocation(ModItems.CART_REMOTE_ANALYZER, 0, new ModelResourceLocation(MODID + ":remote_analyzer", "inventory"));
        setCustomModelResourceLocation(ModItems.CART_REMOTE_MODULE, 0, new ModelResourceLocation(MODID + ":remote_module1", "inventory"));
        setCustomModelResourceLocation(ModItems.CART_REMOTE_MODULE, 1, new ModelResourceLocation(MODID + ":remote_module2", "inventory"));
        setCustomModelResourceLocation(ModItems.CART_REMOTE_MODULE, 2, new ModelResourceLocation(MODID + ":remote_module3", "inventory"));
        setCustomModelResourceLocation(ModItems.COMPUTER_CART, 0, new ModelResourceLocation(MODID + ":computer_cart", "inventory"));
        setCustomModelResourceLocation(ModItems.COMPUTER_CART_CASE, 0, new ModelResourceLocation(MODID + ":computer_cart_case1", "inventory"));
        setCustomModelResourceLocation(ModItems.COMPUTER_CART_CASE, 1, new ModelResourceLocation(MODID + ":computer_cart_case2", "inventory"));
        setCustomModelResourceLocation(ModItems.COMPUTER_CART_CASE, 2, new ModelResourceLocation(MODID + ":computer_cart_case3", "inventory"));
        setCustomModelResourceLocation(ModItems.COMPUTER_CART_CASE, 3, new ModelResourceLocation(MODID + ":computer_cart_case4", "inventory"));
        setCustomModelResourceLocation(ModItems.LINKING_UPGRADE, 0, new ModelResourceLocation(MODID + ":linking_upgrade", "inventory"));
        setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.NETWORK_RAIL), 0, new ModelResourceLocation(MODID + ":network_rail", "inventory"));
        setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.NETWORK_RAIL_BASE), 0, new ModelResourceLocation(MODID + ":network_rail_base", "inventory"));
    }

    @SubscribeEvent
    public void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        CraftingHandler.onCraftingEvent(event);
    }

    @SubscribeEvent
    public void onEntityClick(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getEntityPlayer().inventory.getCurrentItem();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemEntityInteract) {
            if (((ItemEntityInteract) stack.getItem()).onEntityClick(event.getEntityPlayer(), event.getTarget(),
                    stack, ItemEntityInteract.Type.RIGHT_CLICK, event.getHand()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityHit(AttackEntityEvent event) {
        ItemStack stack = event.getEntityPlayer().inventory.getCurrentItem();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemEntityInteract) {
            if (((ItemEntityInteract) stack.getItem()).onEntityClick(event.getEntityPlayer(), event.getTarget(),
                    stack, ItemEntityInteract.Type.LEFT_CLICK, event.getEntityPlayer().getActiveHand()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        ticks++;
        if (ticks < 2) return;
        ticks = 0;
        RemoteExtenderRegister.serverTick();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        NBTTagCompound config = new NBTTagCompound();
        config.setIntArray("remoterange", Settings.RemoteRange);
        ModNetwork.CHANNEL.sendTo(new ConfigSyncMessage(config), (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) return;
        RemoteExtenderRegister.reinit();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) return;
        RemoteExtenderRegister.reinit();
    }

    /*@SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityMinecart) {
            event.addCapability(new ResourceLocation(ComputerCarts.MODID, "remote_cart"), new RemoteCartExtender.Provider());
        }
    }*/
}
