package mods.computercarts.common.recipe;

import net.minecraft.item.crafting.IRecipe;

public abstract class EmblemCrafting implements IRecipe {

    /*@Override
    public boolean matches(InventoryCrafting grid, World p_77569_2_) {
        int e = findEmblem(grid);
        int c = findCrowbar(grid);
        return (findResult(grid) != -1 && ((e != -1 && c == -1) || (e == -1 && c != -1)));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting grid) {
        if (!matches(grid, null)) return null;
        int e = findEmblem(grid);
        int c = findCrowbar(grid);
        int r = findResult(grid);
        ItemStack res = grid.getStackInSlot(r).copy();
        ComputerCartData data = ItemComputerCart.getData(res);
        if (data == null) data = new ComputerCartData();

        if (c != -1)
            data.setEmblem("");
        else if (e != -1) {
            String emid = EmblemToolsServer.getEmblemIdentifier(grid.getStackInSlot(e));
            data.setEmblem(emid);
        }
        ItemComputerCart.setData(res, data);
        return res;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModItems.COMPUTER_CART, 1);
    }

    private int findEmblem(InventoryCrafting grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemEmblem.item)
                return i;
        }
        return -1;
    }

    private int findCrowbar(InventoryCrafting grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (stack != null && (stack.getItem() instanceof IToolCrowbar))
                return i;
        }
        return -1;
    }

    private int findResult(InventoryCrafting grid) {
        int size = grid.getSizeInventory();
        for (int i = 0; i < size; i += 1) {
            ItemStack stack = grid.getStackInSlot(i);
            if (stack != null && stack.getItem() == ModItems.COMPUTER_CART)
                return i;
        }
        return -1;
    }*/

}
