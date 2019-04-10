package nl.requios.effortlessbuilding.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class InventoryHelper {

    public static ItemStack findItemStackInInventory(EntityPlayer player, Block block) {
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof ItemBlock &&
                ((ItemBlock) invStack.getItem()).getBlock().equals(block)) {
                return invStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int findTotalBlocksInInventory(EntityPlayer player, Block block) {
        int total = 0;
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof ItemBlock &&
                ((ItemBlock) invStack.getItem()).getBlock().equals(block)) {
                total += invStack.getCount();
            }
        }
        return total;
    }
}
