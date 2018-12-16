package mods.computercarts.common.recipe;


import mods.computercarts.ComputerCarts;
import mods.computercarts.common.items.ModItems;
import mods.computercarts.common.recipe.event.ComputerCartRomCrafting;
import mods.computercarts.common.recipe.event.CraftingHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class Recipes {
    public static final int SLOT_ROM = 18;

    public static void init() {
        RecipeSorter.register(ComputerCarts.MODID + ":romcrafting", RomCrafting.class, Category.SHAPELESS, "after:minecraft:shaped");
        /*if (Loader.isModLoaded("Railcraft"))
            RecipeSorter.register(ComputerCarts.MODID + ":emblemcrafting", EmblemCrafting.class, Category.SHAPELESS, "after:minecraft:shaped");*/


        /*if (Loader.isModLoaded("Railcraft")) {
            GameRegistry.addShapedRecipe(new ItemStack(ModItems.LINKING_UPGRADE, 1, 0),
                    "XCX",
                    "MAM",
                    "XPX", 'C', ItemCrowbar.getItem(), 'M', items.get("chip1").createItemStack(1), 'A', Item.getItemFromBlock(Blocks.sticky_piston), 'P', items.get("printedCircuitBoard").createItemStack(1));

            ForgeRegistries.RECIPES.register(new EmblemCrafting());
            CraftingHandler.registerNewHandler(new EmblemCraftingEvent());
        }*/

        ForgeRegistries.RECIPES.register(new RomCrafting(new ItemStack(ModItems.COMPUTER_CART), SLOT_ROM));

        CraftingHandler.registerNewHandler(new ComputerCartRomCrafting());
    }
}
