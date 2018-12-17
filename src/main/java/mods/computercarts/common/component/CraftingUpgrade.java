package mods.computercarts.common.component;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mods.computercarts.common.minecart.ComputerCart;
import mods.computercarts.common.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

/*
 * Copy from li.cil.oc.server.component.CraftingUpgrade
 */
public class CraftingUpgrade extends AbstractManagedEnvironment {

    private ComputerCart cart;
    private CraftingInventory cinv;

    public CraftingUpgrade(ComputerCart cart) {
        super();
        this.cart = cart;
        this.cinv = new CraftingInventory();
        this.setNode(Network.newNode(this, Visibility.Network).withComponent("crafting").create());
    }

    @Callback(doc = "function([count:number]):number -- Tries to craft the specified number of items in the top left area of the inventory.")
    public Object[] craft(Context context, Arguments args) {
        int count = args.optInteger(0, Integer.MAX_VALUE);
        return cinv.craft(count);
    }

    private class CraftingInventory extends InventoryCrafting {
        private int possibleAmount = 0;

        public CraftingInventory() {
            super(new Container() {
                @Override
                public boolean canInteractWith(EntityPlayer player) {
                    return true;
                }
            }, 3, 3);
        }

        public Object[] craft(int wcount) {
            load();
            int ccount = 0;
            boolean valid = CraftingManager.findMatchingRecipe(this, CraftingUpgrade.this.cart.world()) != null;
            if (valid) {
                while (ccount < wcount) {
                    IRecipe result = CraftingManager.findMatchingRecipe(this, CraftingUpgrade.this.cart.world());
                    if (result == null) break;
                    ItemStack output = result.getRecipeOutput();
                    if (output.isEmpty()) break;
                    ccount += output.getCount();
                    FMLCommonHandler.instance().firePlayerCraftingEvent(CraftingUpgrade.this.cart.player(), output, this);
                    List<ItemStack> citems = new ArrayList<>();
                    for (int slot = 0; slot < this.getSizeInventory(); slot++) {
                        ItemStack stack = this.getStackInSlot(slot);
                        if (!stack.isEmpty()) this.decrStackSize(slot, 1);
                        if (!stack.isEmpty() && stack.getItem().hasContainerItem(stack)) {
                            ItemStack container = stack.getItem().getContainerItem(stack);
                            if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()) {
                                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(CraftingUpgrade.this.cart.player(), container, EnumHand.MAIN_HAND));
                            } else if (!getStackInSlot(slot).isEmpty()) {
                                citems.add(container);
                            } else {
                                this.setInventorySlotContents(slot, container);
                            }
                        }
                    }

                    save();
                    InventoryUtil.addToPlayerInventory(output, CraftingUpgrade.this.cart.player());
                    for (ItemStack stack : citems) {
                        InventoryUtil.addToPlayerInventory(stack, CraftingUpgrade.this.cart.player());
                    }
                    load();
                }
            }
            return new Object[]{valid, ccount};
        }

        private void load() {
            IInventory hinv = CraftingUpgrade.this.cart.mainInventory();
            this.possibleAmount = Integer.MAX_VALUE;
            for (int slot = 0; slot < this.getSizeInventory(); slot++) {
                ItemStack stack = hinv.getStackInSlot(toParentSlot(slot));
                this.setInventorySlotContents(slot, stack);
                if (!stack.isEmpty()) {
                    this.possibleAmount = Math.min(this.possibleAmount, stack.getCount());
                }
            }
        }

        private void save() {
            IInventory hinv = CraftingUpgrade.this.cart.mainInventory();
            for (int slot = 0; slot < this.getSizeInventory(); slot++) {
                hinv.setInventorySlotContents(toParentSlot(slot), this.getStackInSlot(slot));
            }
        }

        private int toParentSlot(int slot) {
            int col = slot % 3;
            int row = slot / 3;
            return row * 4 + col;
        }
    }

}
