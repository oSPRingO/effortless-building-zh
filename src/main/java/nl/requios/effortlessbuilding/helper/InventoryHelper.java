package nl.requios.effortlessbuilding.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class InventoryHelper {

    public static ItemStack findItemStackInInventory(EntityPlayer player, IBlockState blockState) {
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof ItemBlock &&
                ((ItemBlock) invStack.getItem()).getBlock().equals(blockState.getBlock())) {
                return invStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int findTotalBlocksInInventory(EntityPlayer player, IBlockState blockState) {
        int total = 0;
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (!invStack.isEmpty() && invStack.getItem() instanceof ItemBlock &&
                ((ItemBlock) invStack.getItem()).getBlock().equals(blockState.getBlock())) {
                total += invStack.getCount();
            }
        }
        return total;
    }
}
